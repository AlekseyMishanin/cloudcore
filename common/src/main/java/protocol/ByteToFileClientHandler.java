package protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;
import model.PackageBody;
import model.ProtocolCommand;
import utility.Packages;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Класс инкапсулирует часть протокола, отвечающую за загрузку файла на локальную машину клиента.
 *
 * @author Mishanin Aleksey
 * */
public class ByteToFileClientHandler extends AbstractHandler {

    private PackageBody packageBody;    //объект протокола
    private byte[] dataArr;             //временный буффер для загрузки байт
    private FileOutputStream out;       //байтовый канал для вывода в файл
    private ChannelHandlerContext ctxL;
    private FileTimerTask fileTimerTask;

    public ByteToFileClientHandler(PackageBody packageBody) {
        this.packageBody = packageBody;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        dataArr = new byte[1024];
        this.ctxL = ctx;
        Timer timer = new Timer(true);
        fileTimerTask = new FileTimerTask();
        timer.schedule(fileTimerTask, 1000, 1000);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //если пакет содержит нужную команду и статус
        if(packageBody.getCommand() == ProtocolCommand.FILERESPONSE &&
                packageBody.getStatus() == PackageBody.Status.READFILE) {
            //преобразуем Object к ByteBuf
            ByteBuf buf = ((ByteBuf) msg);
            //если поток пуст
            if(out == null) {
                //создаем новый поток
                out = new FileOutputStream(packageBody.getVariable());
            }
            //если в ByteBuf есть остались байты для чтения
            while (buf.isReadable()){
                //определяем кол-во доступных для чтения байт. Если кол-во доступных байт больше размера массива, то крутимся в цикле пока не прочитаем все байты
                int j = buf.readableBytes() > 1024 ? 1024 : buf.readableBytes();
                //записываем байты во временный буфер
                buf.readBytes(dataArr,0,j);
                //записываем байты в файл
                out.write(dataArr,0,j);
                //уменьшаем длину файла в пакете
                packageBody.setLenghFile(packageBody.getLenghFile()-j);
                fileTimerTask.increment();
            }
            //освобождаем сообщение
            ReferenceCountUtil.release(msg);
            //если не осталось байт для записи
            if (packageBody.getLenghFile() <= 0) {
                long checksum = packageBody.getChecksum();
                String nameFile = packageBody.getVariable();
                //очищаем пакет
                packageBody.clear();
                //закрываем поток
                out.close();
                out = null;
                //проверяем контрольную сумму файла
                if(!(checksum == Packages.getMd5(new File(nameFile)))) {
                    //отправляем в начало ChannelPipeline бит с признаком ошибки в файле
                    ctx.pipeline().fireChannelRead(ctx.alloc().buffer().writeByte(ProtocolCommand.FILEERROR.getData()));
                }
            }
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    /**
     * Класс задания проверяет изменилась ли длина файла в пакете за определенное время. Если не изменилась полагаем, что
     * произошел сбой при передаче данных.
     * */
    class FileTimerTask extends TimerTask{
        private int length1;
        private int length2;

        @Override
        public void run() {

            if(length1 > 0 && packageBody.getStatus() == PackageBody.Status.READFILE && length1 == length2){
                length1 = length2 = 0;
                //очищаем пакет
                packageBody.clear();
                //закрываем поток
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                out = null;
                //отправляем сообщение с ошибкой
                try {
                    Packages.fileError(ctxL.channel());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                length1 = length2;
            }
        }
        public void increment(){
            length2++;
        }
    }


}



package protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;
import model.PackageBody;
import model.ProtocolCommand;

import java.io.FileOutputStream;

/**
 * Класс инкапсулирует часть протокола, отвечающую за загрузку файла на локальную машину клиента.
 *
 * @author Mishanin Aleksey
 * */
public class ByteToFileClientHandler extends AbstractHandler {

    private PackageBody packageBody;    //объект протокола
    private byte[] dataArr;             //временный буффер для загрузки байт
    private FileOutputStream out;       //байтовый канал для вывода в файл

    public ByteToFileClientHandler(PackageBody packageBody) {
        this.packageBody = packageBody;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        dataArr = new byte[1024];
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
            }
            //освобождаем сообщение
            ReferenceCountUtil.release(msg);
            //если не осталось байт для записи
            if (packageBody.getLenghFile() <= 0) {
                //очищаем пакет
                packageBody.clear();
                //закрываем поток
                out.close();
                out = null;
            }
        } else {
            ctx.fireChannelRead(msg);
        }
    }
}

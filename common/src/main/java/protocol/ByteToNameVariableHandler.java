package protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import model.PackageBody;
import model.ProtocolCommand;

/**
 * Класс инкапсулирует часть протокола, отвечающую за операции операции с каталогами на стороне сервера:
 * добавление, удаление, копирование, вырезание, переименование. Также класс читает имя файла.
 *
 * @author Mishanin Aleksey
 * */
public class ByteToNameVariableHandler extends AbstractHandler {

    private PackageBody packageBody;

    public ByteToNameVariableHandler(PackageBody packageBody) {
        this.packageBody = packageBody;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(((packageBody.getCommand() == ProtocolCommand.COPYCATALOG ||
                packageBody.getCommand() == ProtocolCommand.CUTCATALOG ||
                packageBody.getCommand() == ProtocolCommand.RENAMECATALOG) &&
                packageBody.getStatus() == PackageBody.Status.READNAMEOLDPATH) ||
            (packageBody.getCommand() == ProtocolCommand.NEWCATALOG &&
                packageBody.getStatus() == PackageBody.Status.READPATHNEWCATALOG) ||
            (packageBody.getCommand() == ProtocolCommand.DELETECATALOG &&
                    packageBody.getStatus() == PackageBody.Status.READNAMEDELETECATALOG) ||
                ((packageBody.getCommand() == ProtocolCommand.FILE ||
                        packageBody.getCommand() == ProtocolCommand.FILEREQUEST ||
                        packageBody.getCommand() == ProtocolCommand.FILERESPONSE) &&
                        packageBody.getStatus() == PackageBody.Status.READNAMECATALOGFORFILE)) {
            //преобразуем Object к ByteBuf
            ByteBuf buf = ((ByteBuf) msg);
            //если кол-во байт доступных для чтения меньше длины lengthVariable
            if (buf.readableBytes() < packageBody.getLengthVariable()) {
                //прекращаем обработку
                return;
            }
            //создаем временный буфер. Операция не массовая, поэтому можно обойтись локальной переменной
            byte[] data = new byte[packageBody.getLengthVariable()];
            //читаем имя файла во временный буфер
            buf.readBytes(data);
            //в пакете присваиваем новое имя объекта(имя каталога/имя файла)
            packageBody.setVariable(new String(data));
            //исходя из текущего статуса переходим к другому статусу
            switch (packageBody.getStatus()){
                case READPATHNEWCATALOG:
                    packageBody.setStatus(PackageBody.Status.CREATENEWCATALOG);
                    break;
                case READNAMEDELETECATALOG:
                    packageBody.setStatus(PackageBody.Status.DELETECATALOG);
                    break;
                case READNAMECATALOGFORFILE:
                    packageBody.setStatus(PackageBody.Status.READLENGTHNAMEFILE);
                    break;
                case READNAMEOLDPATH:
                    packageBody.setStatus(PackageBody.Status.READLENGTHNEWPATH);
                    break;
            }
        }
        ctx.fireChannelRead(msg);
    }
}

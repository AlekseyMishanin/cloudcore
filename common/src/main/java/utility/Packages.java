package utility;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.FileRegion;
import io.netty.util.ReferenceCountUtil;
import lombok.NonNull;
import model.PackageBody;
import model.ProtocolCommand;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Класс содержит ряд готовых инстументов для отправки/загрузки файлов
 *
 * @author Mishanin Aleksey
 * */
public class Packages {

    /**Класс перечисления определяет получателя файла*/
    private enum Recipient {CLIENT, SERVER;}
    /**
     * Статический метод отправки файла от клиента на сервер
     *
     * @param   channel
     *          канал для отправки байт
     *
     * @param   path
     *          путь до файла
     * */
    public static void sendFromClienToServer(@NonNull final Channel channel, String catalog, Path path) throws IOException, InterruptedException {
        send(channel, catalog, path,Recipient.SERVER);
    }

    /**
     * Статический метод отправки файла от сервера на клиента
     *
     * @param   channel
     *          канал для отправки байт
     *
     * @param   path
     *          путь до файла
     * */
    public static void sendFromServerToClient(@NonNull final Channel channel, String catalog, Path path) throws IOException, InterruptedException {
        send(channel, catalog, path,Recipient.CLIENT);
    }

    /**
     * Служебный метод отправки файла
     *
     * @param   channel
     *          канал для отправки байт
     *
     * @param   path
     *          путь до файла
     *
     * @param   recipient
     *          признак определяет сторону для отправки данных: или server, или client
     * */
    private static void send(@NonNull final Channel channel, String catalog, Path path, Recipient recipient) throws InterruptedException, FileNotFoundException, IOException {

        //исходя из значения аргумента recipient определяем команду протокола
        byte command;
        switch (recipient){
            case SERVER:
                command = ProtocolCommand.FILE.getData();
                break;
            case CLIENT:
                command = ProtocolCommand.FILERESPONSE.getData();
                break;
            default:
                command = ProtocolCommand.FILEERROR.getData();
                break;
        }

        //получаем ByteBufAllocator из канала
        ByteBufAllocator allocator = channel.alloc();
        //возвращаем буфер размерностью 512 байт. Записываем в буфер команду протокола
        ByteBuf buf = allocator.buffer(512).writeByte(command);
        sendIntAndBytes(channel,buf,catalog);
        sendIntAndBytes(channel,buf,path.getFileName().toString());

        //открываем байтовый поток для файла
        try (FileInputStream in = new FileInputStream(path.toFile());) {
            //читаем длину файла
            long len = Files.size(path);
            System.out.println(len);
            //записываем длину файла в ByteBuf
            buf.writeLong(len);
            //отправляем длину файла в канал, увеличиваем счетчик ссылок на 1, ждем отправки всех байт
            channel.writeAndFlush(buf.retain()).await();
            buf.clear();
            //Создаем новый DefaultFileRegion для файла, начиная с 0 и заканчивая в конце файла
            FileRegion region = new DefaultFileRegion(in.getChannel(), 0, path.toFile().length());
            //заливаем в канал байты содержимого файла
            channel.writeAndFlush(region).await();
        }
        ReferenceCountUtil.release(buf);
    }

    /**
     * Статический метод отправки запроса на сервер для скачивания файла
     *
     * @param   channel
     *          канал для отправки байт
     *
     * @param   pathToDownload
     *          путь до файла
     * */
    public static void loadFromServerToClient(@NonNull final Channel channel, Path pathToDownload, String pathToload) throws InterruptedException {

        //получаем ByteBufAllocator из канала
        ByteBufAllocator allocator = channel.alloc();
        //возвращаем буфер размерностью 512 байт. Записываем в буфер команду протокола
        ByteBuf buf = allocator.buffer(512).writeByte(ProtocolCommand.FILEREQUEST.getData());
        //меняем разделители на системные
        String str1 = pathToDownload.toString().replace(File.separator,PackageBody.systemSeparator);
        pathToload = pathToload.replace(File.separator,PackageBody.systemSeparator);
        //отправляем байты в сеть
        sendIntAndBytes(channel, buf, pathToload);
        sendIntAndBytes(channel, buf, str1);
        channel.writeAndFlush(buf).await();
        buf.clear();
    }

    public static void requestCopyCatalog(@NonNull final Channel channel, Path oldCatalog, String newCatalog) throws InterruptedException {

        //получаем ByteBufAllocator из канала
        ByteBufAllocator allocator = channel.alloc();
        //возвращаем буфер размерностью 512 байт. Записываем в буфер команду протокола
        ByteBuf buf = allocator.buffer(512).writeByte(ProtocolCommand.COPYCATALOG.getData());
        //меняем разделители на системные
        String str1 = oldCatalog.toString().replace(File.separator,PackageBody.systemSeparator);
        newCatalog = newCatalog.replace(File.separator,PackageBody.systemSeparator);
        //отправляем байты в сеть
        sendIntAndBytes(channel, buf, str1);
        sendIntAndBytes(channel, buf, newCatalog);
        channel.writeAndFlush(buf).await();
        buf.clear();
    }

    public static void requestCutCatalog(@NonNull final Channel channel, Path oldCatalog, String newCatalog) throws InterruptedException {

        //получаем ByteBufAllocator из канала
        ByteBufAllocator allocator = channel.alloc();
        //возвращаем буфер размерностью 512 байт. Записываем в буфер команду протокола
        ByteBuf buf = allocator.buffer(512).writeByte(ProtocolCommand.CUTCATALOG.getData());
        //меняем разделители на системные
        String str1 = oldCatalog.toString().replace(File.separator,PackageBody.systemSeparator);
        newCatalog = newCatalog.replace(File.separator,PackageBody.systemSeparator);
        //отправляем байты в сеть
        sendIntAndBytes(channel, buf, str1);
        sendIntAndBytes(channel, buf, newCatalog);
        channel.writeAndFlush(buf).await();
        buf.clear();
    }

    public static void requestRenameCatalog(@NonNull final Channel channel, Path oldCatalog, String newCatalog) throws InterruptedException {

        //получаем ByteBufAllocator из канала
        ByteBufAllocator allocator = channel.alloc();
        //возвращаем буфер размерностью 512 байт. Записываем в буфер команду протокола
        ByteBuf buf = allocator.buffer(512).writeByte(ProtocolCommand.RENAMECATALOG.getData());
        //меняем разделители на системные
        String str1 = oldCatalog.toString().replace(File.separator,PackageBody.systemSeparator);
        newCatalog = newCatalog.replace(File.separator,PackageBody.systemSeparator);
        //отправляем байты в сеть
        sendIntAndBytes(channel, buf, str1);
        sendIntAndBytes(channel, buf, newCatalog);
        channel.writeAndFlush(buf).await();
        buf.clear();
    }

    private static void sendIntAndBytes(@NonNull final Channel channel, ByteBuf buf, String value) throws InterruptedException {
        channel.writeAndFlush(buf.retain()).addListener((n) -> { buf.clear();buf.writeInt(value.getBytes().length); }).await();
        channel.writeAndFlush(buf.retain()).addListener((n) -> { buf.clear();buf.writeBytes(value.getBytes()); }).await();
    }

    private static void sendLoginAndPassToChannel(@NonNull final Channel channel, byte command, @NonNull String firstValue, int password) throws InterruptedException {

        ByteBufAllocator allocator = channel.alloc();
        ByteBuf buf = allocator.buffer(512).writeByte(command);
        channel.writeAndFlush(buf.retain()).addListener((n) -> { buf.clear();buf.writeInt(firstValue.getBytes().length); }).await();
        channel.writeAndFlush(buf.retain()).addListener((n) -> { buf.clear();buf.writeBytes(firstValue.getBytes()); }).await();
        channel.writeAndFlush(buf.retain()).addListener((n) -> { buf.clear();buf.writeInt(password); }).await();
        channel.writeAndFlush(buf).await();
        buf.clear();
    }

    private static void sendBooleanToChannel(@NonNull final Channel channel, byte command, boolean bool) throws InterruptedException {
        //получаем ByteBufAllocator из канала
        ByteBufAllocator allocator = channel.alloc();
        //возвращаем буфер размерностью 512 байт. Записываем в буфер команду протокола
        ByteBuf buf = allocator.buffer(512).writeByte(command);
        channel.writeAndFlush(buf.retain()).addListener((n) -> { buf.clear(); buf.writeBoolean(bool); }).await();
        channel.writeAndFlush(buf).await();
        buf.clear();
    }

    private static void sendCommandToChannel(@NonNull final Channel channel, byte command) throws InterruptedException {
        //получаем ByteBufAllocator из канала
        ByteBufAllocator allocator = channel.alloc();
        //возвращаем буфер размерностью 512 байт. Записываем в буфер команду протокола
        ByteBuf buf = allocator.buffer(512).writeByte(command);
        channel.writeAndFlush(buf).addListener((n) -> buf.clear() ).await();
    }

    private static void sendStructureCatalog(@NonNull final Channel channel, byte command, String struct) throws InterruptedException {
        //получаем ByteBufAllocator из канала
        ByteBufAllocator allocator = channel.alloc();
        //возвращаем буфер размерностью 512 байт. Записываем в буфер команду протокола
        ByteBuf buf = allocator.buffer(512).writeByte(command);
        if(struct != null && !struct.isEmpty()){
            channel.writeAndFlush(buf.retain()).addListener((n) -> { buf.clear(); buf.writeInt(struct.getBytes().length); }).await();
            channel.writeAndFlush(buf.retain()).addListener((n) -> { buf.clear(); buf.writeBytes(struct.getBytes()); }).await();
        } else {
            channel.writeAndFlush(buf.retain()).addListener((n) -> { buf.clear(); buf.writeInt(0); }).await();
        }
        channel.writeAndFlush(buf).await();
        buf.clear();
    }

    private static void sendNewCatalog(@NonNull final Channel channel, byte command, String path) throws InterruptedException {

        path.replace(File.separator,PackageBody.systemSeparator);
        //получаем ByteBufAllocator из канала
        ByteBufAllocator allocator = channel.alloc();
        //возвращаем буфер размерностью 512 байт. Записываем в буфер команду протокола
        ByteBuf buf = allocator.buffer(512).writeByte(command);
        sendIntAndBytes(channel, buf,path);
        //sendIntAndBytes(channel, buf,separator);
        channel.writeAndFlush(buf).await();
        buf.clear();
    }

    private static void sendDeleteCatalog(@NonNull final Channel channel, byte command, String catalog) throws InterruptedException {
        //получаем ByteBufAllocator из канала
        ByteBufAllocator allocator = channel.alloc();
        //возвращаем буфер размерностью 512 байт. Записываем в буфер команду протокола
        ByteBuf buf = allocator.buffer(512).writeByte(command);
        sendIntAndBytes(channel, buf,catalog);
        channel.writeAndFlush(buf).await();
        buf.clear();
    }

    public static boolean authorization(@NonNull final Channel channel, @NonNull String login, int password){
        try {
            sendLoginAndPassToChannel(
                    channel,
                    ProtocolCommand.AUTHORIZATION.getData(),
                    login,
                    password
            );
            return true;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean registration(@NonNull final Channel channel, @NonNull String login, int password){
        try {
            sendLoginAndPassToChannel(
                    channel,
                    ProtocolCommand.REGISTRATION.getData(),
                    login,
                    password
            );
            return true;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void sendAuthorizationResponse(@NonNull final Channel channel, boolean bool) throws InterruptedException {

        sendBooleanToChannel(
                channel,
                ProtocolCommand.AUTHRESPONSE.getData(),
                bool
        );
    }

    public static void sendRegistrationResponse(@NonNull final Channel channel, boolean bool) throws InterruptedException {

        sendBooleanToChannel(
                channel,
                ProtocolCommand.REGRESPONSE.getData(),
                bool
        );
    }

    public static void requestDirectoryStructure(@NonNull final Channel channel) throws InterruptedException {

        sendCommandToChannel(
                channel,
                ProtocolCommand.STRUCTUREREQUEST.getData()
        );
    }

    public static void responseDirectoryStructure(@NonNull final Channel channel, String struct) throws InterruptedException {

        sendStructureCatalog(
                channel,
                ProtocolCommand.STRUCTURERESPONSE.getData(),
                struct
        );
    }

    public static void requestCteareNewCatalog(@NonNull final Channel channel, String path) throws InterruptedException {

        sendNewCatalog(
                channel,
                ProtocolCommand.NEWCATALOG.getData(),
                path
        );
    }

    public static void updateStructure(@NonNull final Channel channel) throws InterruptedException {
        sendCommandToChannel(
                channel,
                ProtocolCommand.UPDATESTRUCTURE.getData()
        );
    }

    public static void requestDeleteCatalog(@NonNull final Channel channel, String catalog) throws InterruptedException {
        sendDeleteCatalog(
                channel,
                ProtocolCommand.DELETECATALOG.getData(),
                catalog
        );
    }
}

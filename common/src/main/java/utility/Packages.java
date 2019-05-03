package utility;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.FileRegion;
import io.netty.util.ReferenceCountUtil;
import lombok.NonNull;
import model.PackageTransport;
import model.ProtocolCommand;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;

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
     * @param   pack
     *          облегченный вариант пакета с данными
     * */
    public static void sendFromClienToServer(@NonNull final Channel channel, PackageTransport pack) throws IOException, InterruptedException {
        send(channel,pack,Recipient.SERVER);
    }

    /**
     * Статический метод отправки файла от сервера на клиента
     *
     * @param   channel
     *          канал для отправки байт
     *
     * @param   pack
     *          облегченный вариант пакета с данными
     * */
    public static void sendFromServerToClient(@NonNull final Channel channel, PackageTransport pack) throws IOException, InterruptedException {
        send(channel,pack,Recipient.CLIENT);
    }

    /**
     * Служебный метод отправки файла
     *
     * @param   channel
     *          канал для отправки байт
     *
     * @param   pack
     *          облегченный вариант пакета с данными
     *
     * @param   recipient
     *          признак определяет сторону для отправки данных: или server, или client
     * */
    private static void send(@NonNull final Channel channel, PackageTransport pack, Recipient recipient) throws InterruptedException, FileNotFoundException, IOException {

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

        sendHeaderToChannel(
                channel,
                command,
                pack.getUser(),
                pack.getPathToFile().getFileName().toString()
        );

        //получаем ByteBufAllocator из канала
        ByteBufAllocator allocator = channel.alloc();
        //возвращаем буфер размерностью 512 байт. Записываем в буфер команду протокола
        ByteBuf buf = allocator.buffer(512).writeByte(command);
        //открываем байтовый поток для файла
        try (FileInputStream in = new FileInputStream(pack.getPathToFile().toFile());) {
            //читаем длину файла
            long len = Files.size(pack.getPathToFile());
            //записываем длину файла в ByteBuf
            buf.writeLong(len);
            //отправляем длину файла в канал, увеличиваем счетчик ссылок на 1, ждем отправки всех байт
            channel.writeAndFlush(buf.retain()).await();
            buf.clear();
            //Создаем новый DefaultFileRegion для файла, начиная с 0 и заканчивая в конце файла
            FileRegion region = new DefaultFileRegion(in.getChannel(), 0, pack.getPathToFile().toFile().length());
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
     * @param   pack
     *          облегченный вариант пакета с данными
     * */
    public static void loadFromServerToClient(@NonNull final Channel channel, PackageTransport pack) throws InterruptedException {

        sendHeaderToChannel(
                channel,
                ProtocolCommand.FILEREQUEST.getData(),
                pack.getUser(),
                pack.getPathToFile().getFileName().toString()
        );
    }

    /**
     * Служебный метод для отправки повторяющейся части заголовка пакета в сеть
     *
     * @param   channel
     *          канал для отправки байт
     *
     * @param   command
     *          комадна протокола
     *
     * @param   firstValue
     *          первый элемент заголовка (имя пользователя или логин)
     *
     * @param   secondValue
     *          второй элемент заголовка (имя файла или пароль)
     * */
    private static void sendHeaderToChannel(@NonNull final Channel channel, byte command, @NonNull String firstValue, @NonNull String secondValue) throws InterruptedException {
        //получаем ByteBufAllocator из канала
        ByteBufAllocator allocator = channel.alloc();
        //возвращаем буфер размерностью 512 байт. Записываем в буфер команду протокола
        ByteBuf buf = allocator.buffer(512).writeByte(command);
        channel.writeAndFlush(buf.retain()).addListener((n) -> { buf.clear();buf.writeInt(firstValue.getBytes().length); }).await();
        //отправляем байты по каналу, увеличиваем счетчик ссылок на буфер на 1. Навешиваем обработчик успешной отправки байт: сбросить в буфере указатели чтения/записи; записать в буфер имя пользователя. Подождать отправки всех байт.
        channel.writeAndFlush(buf.retain()).addListener((n) -> { buf.clear();buf.writeBytes(firstValue.getBytes()); }).await();
        //отправляем байты по каналу, увеличиваем счетчик ссылок на буфер на 1. Навешиваем обработчик успешной отправки байт: сбросить в буфере указатели чтения/записи; записать в буфер длину имени файла. Подождать отправки всех байт.
        channel.writeAndFlush(buf.retain()).addListener((n) -> { buf.clear();buf.writeInt(secondValue.getBytes().length); }).await();
        //отправляем байты по каналу, увеличиваем счетчик ссылок на буфер на 1. Навешиваем обработчик успешной отправки байт: сбросить в буфере указатели чтения/записи; записать в буфер имя файла. Подождать отправки всех байт.
        channel.writeAndFlush(buf.retain()).addListener((n) -> { buf.clear();buf.writeBytes(secondValue.getBytes()); }).await();
        //отправляем байты по каналу, увеличиваем счетчик ссылок на буфер на 1. Подождать отправки всех байт.
        channel.writeAndFlush(buf.retain()).await();
        buf.clear();
        ReferenceCountUtil.release(buf);
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
        //получаем ByteBufAllocator из канала
        ByteBufAllocator allocator = channel.alloc();
        //возвращаем буфер размерностью 512 байт. Записываем в буфер команду протокола
        ByteBuf buf = allocator.buffer(512).writeByte(command);
        sendIntAndBytes(channel, buf,path);
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
}

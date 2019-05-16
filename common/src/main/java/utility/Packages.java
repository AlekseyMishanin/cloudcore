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

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
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

    /**
     * Статический метод отправки запроса на сервер на копирование каталога
     *
     * @param   channel
     *          канал для отправки байт
     *
     * @param   oldCatalog
     *          копируемый каталог
     * @param   newCatalog
     *          новый каталог
     * */
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

    /**
     * Статический метод отправки запроса на сервер на вырезание каталога
     *
     * @param   channel
     *          канал для отправки байт
     *
     * @param   oldCatalog
     *          каталог который нужно вырезать
     * @param   newCatalog
     *          новый каталог
     * */
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

    /**
     * Статический метод отправки запроса на сервер на переименование каталога
     *
     * @param   channel
     *          канал для отправки байт
     *
     * @param   oldCatalog
     *          имя старого каталога
     * @param   newCatalog
     *          имя нового каталога
     * */
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

    /**
     * Служебный метод для отправки пары: длина "объекта"(например, имя пользователя или каталога) и собственно сам объект
     *
     * @param   channel
     *          канал для отправки байт
     *
     * @param   buf
     *          ByteBuf для записи байт
     * @param   value
     *          отправляемый "объект"
     * */
    private static void sendIntAndBytes(@NonNull final Channel channel, ByteBuf buf, String value) throws InterruptedException {
        //отправляем длину объекта
        channel.writeAndFlush(buf.retain()).addListener((n) -> { buf.clear();buf.writeInt(value.getBytes().length); }).await();
        //отправляем содержимое объекта
        channel.writeAndFlush(buf.retain()).addListener((n) -> { buf.clear();buf.writeBytes(value.getBytes()); }).await();
    }

    /**
     * Служебный метод для отправки пары логин/пароль
     *
     * @param   channel
     *          канал для отправки байт
     *
     * @param   command
     *          командный байт
     * @param   firstValue
     *          логин
     * @param   password
     *          пароль
     * */
    private static void sendLoginAndPassToChannel(@NonNull final Channel channel, byte command, @NonNull String firstValue, int password) throws InterruptedException, BadPaddingException, IllegalBlockSizeException {

        ByteBufAllocator allocator = channel.alloc();
        ByteBuf buf = allocator.buffer(512).writeByte(command);
        channel.writeAndFlush(buf.retain()).addListener((n) -> { buf.clear();buf.writeInt(firstValue.getBytes().length); }).await();
        channel.writeAndFlush(buf.retain()).addListener((n) -> { buf.clear();buf.writeBytes(firstValue.getBytes()); }).await();
        channel.writeAndFlush(buf.retain()).addListener((n) -> { buf.clear();buf.writeInt(password); }).await();
        channel.writeAndFlush(buf).await();
        buf.clear();
    }

    /**
     * Служебный метод для отправки boolean в сеть
     *
     * @param   channel
     *          канал для отправки байт
     *
     * @param   command
     *          командный байт
     * @param   bool
     *          значение логического типа
     * */
    private static void sendBooleanToChannel(@NonNull final Channel channel, byte command, boolean bool) throws InterruptedException {
        //получаем ByteBufAllocator из канала
        ByteBufAllocator allocator = channel.alloc();
        //возвращаем буфер размерностью 512 байт. Записываем в буфер команду протокола
        ByteBuf buf = allocator.buffer(512).writeByte(command);
        channel.writeAndFlush(buf.retain()).addListener((n) -> { buf.clear(); buf.writeBoolean(bool); }).await();
        channel.writeAndFlush(buf).await();
        buf.clear();
    }

    /**
     * Служебный метод для отправки командного байта в сеть
     *
     * @param   channel
     *          канал для отправки байт
     *
     * @param   command
     *          командный байт
     * */
    private static void sendCommandToChannel(@NonNull final Channel channel, byte command) throws InterruptedException {
        //получаем ByteBufAllocator из канала
        ByteBufAllocator allocator = channel.alloc();
        //возвращаем буфер размерностью 512 байт. Записываем в буфер команду протокола
        ByteBuf buf = allocator.buffer(512).writeByte(command);
        channel.writeAndFlush(buf).addListener((n) -> buf.clear() ).await();
    }

    /**
     * Служебный метод для отправки структуры каталогов в сеть
     *
     * @param   channel
     *          канал для отправки байт
     *
     * @param   command
     *          командный байт
     * @param   struct
     *          строка со структурой каталогов
     * */
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

    /**
     * Служебный метод для отправки нового каталога в сеть
     *
     * @param   channel
     *          канал для отправки байт
     *
     * @param   command
     *          командный байт
     * @param   path
     *          путь до нового каталога
     * */
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

    /**
     * Служебный метод для отправки удаляемого каталога в сеть
     *
     * @param   channel
     *          канал для отправки байт
     *
     * @param   command
     *          командный байт
     * @param   catalog
     *          путь до удаляемого каталога
     * */
    private static void sendDeleteCatalog(@NonNull final Channel channel, byte command, String catalog) throws InterruptedException {
        //получаем ByteBufAllocator из канала
        ByteBufAllocator allocator = channel.alloc();
        //возвращаем буфер размерностью 512 байт. Записываем в буфер команду протокола
        ByteBuf buf = allocator.buffer(512).writeByte(command);
        sendIntAndBytes(channel, buf,catalog);
        channel.writeAndFlush(buf).await();
        buf.clear();
    }

    /**
     * Метод для отправки запроса на авторизацию на сервере
     *
     * @param   channel
     *          канал для отправки байт
     *
     * @param   login
     *          логин
     * @param   password
     *          пароль
     * @return  true - если запрос на авторизацию успешно отправлен
     * */
    public static boolean authorization(@NonNull final Channel channel, @NonNull String login, int password){
        try {
            sendLoginAndPassToChannel(
                    channel,
                    ProtocolCommand.AUTHORIZATION.getData(),
                    login,
                    password
            );
            return true;
        } catch (InterruptedException|BadPaddingException|IllegalBlockSizeException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Метод для отправки запроса на регистрацию на сервере
     *
     * @param   channel
     *          канал для отправки байт
     *
     * @param   login
     *          логин
     * @param   password
     *          пароль
     * @return  true - если запрос на регистрацию успешно отправлен
     * */
    public static boolean registration(@NonNull final Channel channel, @NonNull String login, int password){
        try {
            sendLoginAndPassToChannel(
                    channel,
                    ProtocolCommand.REGISTRATION.getData(),
                    login,
                    password
            );
            return true;
        } catch (InterruptedException|BadPaddingException|IllegalBlockSizeException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Метод для отправки ответа сервена на запрос авторизации
     *
     * @param   channel
     *          канал для отправки байт
     *
     * @param   bool
     *          результат авторизации
     * */
    public static void sendAuthorizationResponse(@NonNull final Channel channel, boolean bool) throws InterruptedException {

        sendBooleanToChannel(
                channel,
                ProtocolCommand.AUTHRESPONSE.getData(),
                bool
        );
    }

    /**
     * Метод для отправки ответа сервена на запрос регистрации
     *
     * @param   channel
     *          канал для отправки байт
     *
     * @param   bool
     *          результат регистрации
     * */
    public static void sendRegistrationResponse(@NonNull final Channel channel, boolean bool) throws InterruptedException {

        sendBooleanToChannel(
                channel,
                ProtocolCommand.REGRESPONSE.getData(),
                bool
        );
    }

    /**
     * Метод для отправки запроса на получение новой структуры каталогов
     *
     * @param   channel
     *          канал для отправки байт
     * */
    public static void requestDirectoryStructure(@NonNull final Channel channel) throws InterruptedException {

        sendCommandToChannel(
                channel,
                ProtocolCommand.STRUCTUREREQUEST.getData()
        );
    }

    /**
     * Метод для отправки ответа сервера на запрос новой структуры каталогов
     *
     * @param   channel
     *          канал для отправки байт
     *
     * @param   struct
     *          строка с новой структурой каталогов
     * */
    public static void responseDirectoryStructure(@NonNull final Channel channel, String struct) throws InterruptedException {

        sendStructureCatalog(
                channel,
                ProtocolCommand.STRUCTURERESPONSE.getData(),
                struct
        );
    }

    /**
     * Метод для отправки запроса на создание нового каталога
     *
     * @param   channel
     *          канал для отправки байт
     *
     * @param   path
     *          путь нового каталога
     * */
    public static void requestCteareNewCatalog(@NonNull final Channel channel, String path) throws InterruptedException {

        sendNewCatalog(
                channel,
                ProtocolCommand.NEWCATALOG.getData(),
                path
        );
    }

    /**
     * Метод для уведомления клиента об обновлении структуры каталогов
     *
     * @param   channel
     *          канал для отправки байт
     * */
    public static void updateStructure(@NonNull final Channel channel) throws InterruptedException {
        sendCommandToChannel(
                channel,
                ProtocolCommand.UPDATESTRUCTURE.getData()
        );
    }

    /**
     * Метод для отправки запроса на удаление каталога
     *
     * @param   channel
     *          канал для отправки байт
     *
     * @param   catalog
     *          путь удаляемого каталога
     * */
    public static void requestDeleteCatalog(@NonNull final Channel channel, String catalog) throws InterruptedException {
        sendDeleteCatalog(
                channel,
                ProtocolCommand.DELETECATALOG.getData(),
                catalog
        );
    }

    /**
     * Метод используемый на сервере для уведомления клиента об отказе в выполении операции
     *
     * @param   channel
     *          канал для отправки байт
     * */
    public static void deniedInAction(@NonNull final Channel channel) throws InterruptedException {
        sendCommandToChannel(
                channel,
                ProtocolCommand.DENIED.getData()
        );
    }
}

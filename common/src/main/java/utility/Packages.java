package utility;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.FileRegion;
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

    /**
     * Статический метод отправки файла от клиента на сервер
     *
     * @param   channel
     *          канал для отправки байт
     *
     * @param   pack
     *          облегченный вариант пакета с данными
     * */
    public static void sendFromClienToServer(Channel channel, PackageTransport pack) throws IOException, InterruptedException {
        send(channel,pack,"server");
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
    public static void sendFromServerToClient(Channel channel, PackageTransport pack) throws IOException, InterruptedException {
        send(channel,pack,"client");
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
     * @param   to
     *          признак определяет сторону для отправки данных: или server, или client
     * */
    private static void send(Channel channel, PackageTransport pack, String to) throws InterruptedException, FileNotFoundException, IOException {

        //исходя из значения аргумента "to" определяем команду протокола
        byte command = to.equals("server") ? ProtocolCommand.FILE.getData() : to.equals("client") ? ProtocolCommand.FILERESPONSE.getData() : ProtocolCommand.FILEERROR.getData();
        //получаем ByteBufAllocator из канала
        ByteBufAllocator allocator = channel.alloc();
        //возвращаем буфер размерностью 512 байт. Записываем в буфер команду протокола
        ByteBuf buf = allocator.buffer(512).writeByte(command);

        if(to.equals("server") || to.equals("client")){
            //отправляем байты по каналу, увеличиваем счетчик ссылок на буфер на 1. Навешиваем обработчик успешной отправки байт: сбросить в буфере указатели чтения/записи; записать в буфер длину имени пользователя. Подождать отправки всех байт.
            channel.writeAndFlush(buf.retain()).addListener((n)->{buf.clear(); buf.writeInt(pack.getUser().getBytes().length);}).await();
            //отправляем байты по каналу, увеличиваем счетчик ссылок на буфер на 1. Навешиваем обработчик успешной отправки байт: сбросить в буфере указатели чтения/записи; записать в буфер имя пользователя. Подождать отправки всех байт.
            channel.writeAndFlush(buf.retain()).addListener((n)->{buf.clear(); buf.writeBytes(pack.getUser().getBytes());}).await();
            //отправляем байты по каналу, увеличиваем счетчик ссылок на буфер на 1. Навешиваем обработчик успешной отправки байт: сбросить в буфере указатели чтения/записи; записать в буфер длину имени файла. Подождать отправки всех байт.
            channel.writeAndFlush(buf.retain()).addListener((n)->{buf.clear(); buf.writeInt(pack.getPathToFile().getFileName().toString().length());}).await();
            //отправляем байты по каналу, увеличиваем счетчик ссылок на буфер на 1. Навешиваем обработчик успешной отправки байт: сбросить в буфере указатели чтения/записи; записать в буфер имя файла. Подождать отправки всех байт.
            channel.writeAndFlush(buf.retain()).addListener((n)->{buf.clear(); buf.writeBytes(pack.getPathToFile().getFileName().toString().getBytes());}).await();
            //отправляем байты по каналу, увеличиваем счетчик ссылок на буфер на 1. Подождать отправки всех байт.
            channel.writeAndFlush(buf.retain()).await();
            buf.clear();

            //открываем байтовый поток для файла
            try (FileInputStream in = new FileInputStream(pack.getPathToFile().toFile());)
            {
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
        }
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
    public static void loadFromServerToClient(Channel channel, PackageTransport pack) throws InterruptedException {

        byte command = ProtocolCommand.FILEREQUEST.getData();
        ByteBufAllocator allocator = channel.alloc();
        ByteBuf buf = allocator.buffer(512).writeByte(command);
        channel.writeAndFlush(buf.retain()).addListener((n)->{buf.clear(); buf.writeInt(pack.getUser().getBytes().length);}).await();
        channel.writeAndFlush(buf.retain()).addListener((n)->{buf.clear(); buf.writeBytes(pack.getUser().getBytes());}).await();
        channel.writeAndFlush(buf.retain()).addListener((n)->{buf.clear(); buf.writeInt(pack.getPathToFile().getFileName().toString().length());}).await();
        channel.writeAndFlush(buf.retain()).addListener((n)->{buf.clear(); buf.writeBytes(pack.getPathToFile().getFileName().toString().getBytes());}).await();
        channel.writeAndFlush(buf.retain()).await();
        buf.clear();
    }
}

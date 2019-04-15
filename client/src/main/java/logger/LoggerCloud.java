package logger;


import lombok.NoArgsConstructor;

import java.io.IOException;
import java.util.logging.*;


/**
 * Класс Logger. В перспективе бужет записывать стеки ошибок и прочие системные сообщения.
 * */
@NoArgsConstructor
public final class LoggerCloud {

    public static final Logger LOGGER = Logger.getLogger("");
    {
        LOGGER.setLevel(Level.SEVERE);
        Handler handler = null;
        try {
            handler = new FileHandler("logSendMessage.log", 100000,1,true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        handler.setLevel(Level.ALL);
        handler.setFormatter(new SimpleFormatter());
        LOGGER.addHandler(handler);
    }
}

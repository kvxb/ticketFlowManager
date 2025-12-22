package io;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.io.File;
import java.io.IOException;

public class IOUtil {
    private static final String INPUT_USERS_FIELD = "input/database/users.json";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private IOUtil() {

    }

    public static List<CommandInput> readCommands(String inputPath) throws IOException{
        File inputFile = new File(inputPath);

        return MAPPER.readerForListOf(CommandInput.class)
                     .readValue(inputFile);
    }
}

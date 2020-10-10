package tech.ypsilon.bbbot.util;

import java.util.ArrayList;
import java.util.List;

public class StringUtil {

    public static List<String> parseString(final String toParse) {
        List<String> parsed = new ArrayList<>();
        boolean isInString = false;
        StringBuilder builder = new StringBuilder();
        for (char c : toParse.trim().toCharArray()) {
            switch (c) {
                case '"': {
                    if(isInString) {
                        isInString = false;
                        parsed.add(builder.toString());
                        builder = new StringBuilder();
                    } else {
                        isInString = true;
                    }
                }break;
                case ' ': {
                    if(!isInString) {
                        if(builder.length() > 0)
                            parsed.add(builder.toString());
                        builder = new StringBuilder();
                        break;
                    }

                }
                default: {
                    builder.append(c);
                }
            }
        }
        parsed.add(builder.toString());
        return parsed;
    }

}

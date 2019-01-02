package ski.crunch.activity.parser;
import ski.crunch.activity.model.processor.ActivityHolder;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

public interface ActivityHolderAdapter {

    ActivityHolder convert(InputStream is) throws ParseException, IOException;
}

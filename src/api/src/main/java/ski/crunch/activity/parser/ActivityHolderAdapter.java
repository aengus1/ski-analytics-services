package ski.crunch.activity.parser;
import ski.crunch.activity.processor.model.ActivityHolder;

import java.io.IOException;
import java.io.InputStream;
import ski.crunch.utils.ParseException;

public interface ActivityHolderAdapter {

    ActivityHolder convert(InputStream is) throws ParseException, IOException;
}

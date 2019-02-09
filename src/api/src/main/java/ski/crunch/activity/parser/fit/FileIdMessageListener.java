package ski.crunch.activity.parser.fit;

import com.garmin.fit.Field;
import com.garmin.fit.FileIdMesg;
import com.garmin.fit.FileIdMesgListener;
import com.garmin.fit.Manufacturer;
import ski.crunch.activity.processor.model.ActivityHolder;

import java.util.Date;

/**
 * Get Data capture Manufacturer, Product and File Creation Timestamp
 *
 *
 * The file_id message identifies the format/content of the FIT file (type field) and the combination of fields
 * provides a globally unique identifier for the file. Each FIT file should contain one and only one file_id message.
 * If the combination of type, manufacturer, product and serial_number is insufficient, for example on a device
 * supporting multiple device files, the time_created or number fields must be populated to differentiate the files.
 */
public class FileIdMessageListener extends AbstractMesgListener implements FileIdMesgListener {

    public FileIdMessageListener(ActivityHolder holder) {
        super(holder);
    }

    @Override
    public void onMesg(FileIdMesg mesg) {

        logger.info("parsing file id message");
        try {

            for (Field f : mesg.getFields()) {

                if (f.getName().equals("time_created")) {
                    Date d = new Date(((long) f.getValue() * 1000) + offset);
                    logger.debug("time created: " + super.sourceFormat.format(d) + " " + f.getType());
                    activityHolder.setCreatedTs(sourceFormat.format(d));
                }
                if (f.getName().equals("manufacturer")) {
                    logger.debug( "manufacturer: "
                            + Manufacturer.getStringFromValue((int) f.getValue()));
                    activityHolder.setManufacturer(
                            Manufacturer.getStringFromValue((int) f.getValue()));
                }

                if (f.getName().equals("product")) {
                    logger.debug("product: " + (int) f.getValue());
                    activityHolder.setProduct((int) f.getValue());
                }

            }
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error("exception in file id message convert", ex);
        }
    }
}

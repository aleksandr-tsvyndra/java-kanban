package tracker.httptaskserver.typeadapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDateTime;

public class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {
    @Override
    public void write(final JsonWriter jsonWriter, final LocalDateTime localDateTime) throws IOException {
        jsonWriter.value(String.valueOf(localDateTime));
    }

    @Override
    public LocalDateTime read(final JsonReader jsonReader) throws IOException {
        String localDateTime = jsonReader.nextString();
        if (localDateTime.equals("null")) {
            return null;
        }
        return LocalDateTime.parse(localDateTime);
    }
}

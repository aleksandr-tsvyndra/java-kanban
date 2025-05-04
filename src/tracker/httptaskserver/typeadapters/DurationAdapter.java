package tracker.httptaskserver.typeadapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.Duration;

public class DurationAdapter extends TypeAdapter<Duration> {
    @Override
    public void write(final JsonWriter jsonWriter, final Duration duration) throws IOException {
        jsonWriter.value(String.valueOf(duration));
    }

    @Override
    public Duration read(final JsonReader jsonReader) throws IOException {
        String duration = jsonReader.nextString();
        if (duration.equals("null")) {
            return null;
        }
        return Duration.parse(duration);
    }
}

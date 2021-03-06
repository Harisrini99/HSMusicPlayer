package music.hs.com.materialmusicv2.glide.artist;

import androidx.annotation.NonNull;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.data.DataFetcher;

import java.nio.ByteBuffer;

import music.hs.com.materialmusicv2.HSMusicApplication;
import music.hs.com.materialmusicv2.objects.Artist;

import static music.hs.com.materialmusicv2.glide.artist.LoaderUtils.getArtistImageByteArrayFromName;
import static music.hs.com.materialmusicv2.utils.misc.Etc.isConnectedToWifi;

public class ArtistDataFetcher implements DataFetcher<ByteBuffer> {

    private final Artist model;

    ArtistDataFetcher(Artist model) {
        this.model = model;
    }

    @Override
    public void loadData(@NonNull Priority priority, @NonNull DataCallback<? super ByteBuffer> callback) {
        byte[] data = null;
        ByteBuffer byteBuffer;
        if ((HSMusicApplication.getInstance().getPreferenceUtils().getDownloadOnlyOnWifi() && isConnectedToWifi()) || !HSMusicApplication.getInstance().getPreferenceUtils().getDownloadOnlyOnWifi()) {
            data = getArtistImageByteArrayFromName(model.getName());
        }
        byteBuffer = ByteBuffer.wrap(data);
        callback.onDataReady(byteBuffer);
    }

    @Override
    public void cleanup() {
    }

    @Override
    public void cancel() {

    }

    @NonNull
    @Override
    public Class<ByteBuffer> getDataClass() {
        return ByteBuffer.class;
    }

    @NonNull
    @Override
    public DataSource getDataSource() {
        return DataSource.REMOTE;
    }
}

package sapphire.appexamples.minnietwitter.device.twimight.util;

import android.content.SearchRecentSuggestionsProvider;

public class TwimightSuggestionProvider extends SearchRecentSuggestionsProvider {
	
    public final static String AUTHORITY = "sapphire.appexamples.minnietwitter.device.twimight.TwimightSuggestionProvider";
    public final static int MODE = DATABASE_MODE_QUERIES;

    public TwimightSuggestionProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }
}

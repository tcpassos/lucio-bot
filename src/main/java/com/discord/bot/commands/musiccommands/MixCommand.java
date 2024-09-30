package com.discord.bot.commands.musiccommands;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.discord.bot.commands.ISlashCommand;
import com.discord.bot.dto.spotify.ArtistDto;
import com.discord.bot.dto.spotify.TrackDto;
import com.discord.bot.service.MessageService;
import com.discord.bot.service.SpotifyService;
import com.discord.bot.service.audioplayer.PlayerManagerService;

import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@AllArgsConstructor
public class MixCommand implements ISlashCommand {

    public static final String[] genres = {"acoustic", "afrobeat", "alt-rock", "alternative", "ambient", "anime", "black-metal", "bluegrass", "blues", "bossanova", "brazil", "breakbeat", "british", "cantopop", "chicago-house", "children", "chill", "classical", "club", "comedy", "country", "dance", "dancehall", "death-metal", "deep-house", "detroit-techno", "disco", "disney", "drum-and-bass", "dub", "dubstep", "edm", "electro", "electronic", "emo", "folk", "forro", "french", "funk", "garage", "german", "gospel", "goth", "grindcore", "groove", "grunge", "guitar", "happy", "hard-rock", "hardcore", "hardstyle", "heavy-metal", "hip-hop", "holidays", "honky-tonk", "house", "idm", "indian", "indie", "indie-pop", "industrial", "iranian", "j-dance", "j-idol", "j-pop", "j-rock", "jazz", "k-pop", "kids", "latin", "latino", "malay", "mandopop", "metal", "metal-misc", "metalcore", "minimal-techno", "movies", "mpb", "new-age", "new-release", "opera", "pagode", "party", "philippines-opm", "piano", "pop", "pop-film", "post-dubstep", "power-pop", "progressive-house", "psych-rock", "punk", "punk-rock", "r-n-b", "rainy-day", "reggae", "reggaeton", "road-trip", "rock", "rock-n-roll", "rockabilly", "romance", "sad", "salsa", "samba", "sertanejo", "show-tunes", "singer-songwriter", "ska", "sleep", "songwriter", "soul", "soundtracks", "spanish", "study", "summer", "swedish", "synth-pop", "tango", "techno", "trance", "trip-hop", "turkish", "work-out", "world-music"};

    MessageService messageService;
    SpotifyService spotifyService;
    PlayerManagerService playerManagerService;

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        var command = event.getSubcommandName();
        var amountOption = event.getOption("amount");
        int amount = amountOption != null ? amountOption.getAsInt() : 5;
        event.deferReply().queue();

        List<TrackDto> recommendations = switch (command) {
            case "artists" -> getRecommendationsForArtists(event, amount);
            case "genres" -> getRecommendationsForGenres(event, amount);
            case "tracks" -> getRecommendationsForTracks(event, amount);
            default -> new ArrayList<>();
        };
        
        var songs = recommendations.stream()
                                   .map(track -> track.getExternalUrls().getSpotify())
                                   .toList();
        if (songs.isEmpty()) {
            event.getHook().sendMessageEmbeds(messageService.getEmbedError("bot.song.nomatches").build())
                 .setEphemeral(true)
                 .queue();
            return;
        }

        if (playerManagerService.joinAudioChannel(event)) {
            for (String song : songs) {
                playerManagerService.loadAndPlayMusic(event, song);
            }
        }
    }

    private List<TrackDto> getRecommendationsForArtists(SlashCommandInteractionEvent event, int amount) {
        List<String> artists = new ArrayList<>();
        var artist1Option = event.getOption("artist1");
        var artist2Option = event.getOption("artist2");
        var artist3Option = event.getOption("artist3");
        var artist4Option = event.getOption("artist4");
        var artist5Option = event.getOption("artist5");
        if (artist1Option != null) artists.add(artist1Option.getAsString().trim());
        if (artist2Option != null) artists.add(artist2Option.getAsString().trim());
        if (artist3Option != null) artists.add(artist3Option.getAsString().trim());
        if (artist4Option != null) artists.add(artist4Option.getAsString().trim());
        if (artist5Option != null) artists.add(artist5Option.getAsString().trim());

        List<String> artistIds = artists.stream()
                                        .map(artist -> spotifyService.searchArtist(artist))
                                        .filter(Optional::isPresent)
                                        .map(Optional::get)
                                        .map(ArtistDto::getId)
                                        .toList();

        return spotifyService.getRecommendationsForArtists(artistIds, amount);
    }

    private List<TrackDto> getRecommendationsForGenres(SlashCommandInteractionEvent event, int amount) {
        List<String> genres = new ArrayList<>();
        var genre1Option = event.getOption("genre1");
        var genre2Option = event.getOption("genre2");
        var genre3Option = event.getOption("genre3");
        var genre4Option = event.getOption("genre4");
        var genre5Option = event.getOption("genre5");
        if (genre1Option != null) genres.add(genre1Option.getAsString().trim());
        if (genre2Option != null) genres.add(genre2Option.getAsString().trim());
        if (genre3Option != null) genres.add(genre3Option.getAsString().trim());
        if (genre4Option != null) genres.add(genre4Option.getAsString().trim());
        if (genre5Option != null) genres.add(genre5Option.getAsString().trim());

        return spotifyService.getRecommendationsForGenres(genres, amount);
    }

    private List<TrackDto> getRecommendationsForTracks(SlashCommandInteractionEvent event, int amount) {
        List<String> tracks = new ArrayList<>();
        var track1Option = event.getOption("track1");
        var track2Option = event.getOption("track2");
        var track3Option = event.getOption("track3");
        var track4Option = event.getOption("track4");
        var track5Option = event.getOption("track5");
        if (track1Option != null) tracks.add(track1Option.getAsString().trim());
        if (track2Option != null) tracks.add(track2Option.getAsString().trim());
        if (track3Option != null) tracks.add(track3Option.getAsString().trim());
        if (track4Option != null) tracks.add(track4Option.getAsString().trim());
        if (track5Option != null) tracks.add(track5Option.getAsString().trim());

        List<String> trackIds = tracks.stream().map(track -> spotifyService.searchTrack(track))
                                               .filter(Optional::isPresent)
                                               .map(Optional::get)
                                               .map(TrackDto::getId)
                                               .toList();

        return spotifyService.getRecommendationsForTracks(trackIds, amount);
    }
}

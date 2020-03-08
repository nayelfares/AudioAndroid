package com.media.myapplication;

import android.os.Environment;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class AudioMerge {
   public static void concat2Audio(String firstFile,String secondFile){

        Movie movieA = null;
        Movie movieB = null;
        try {
            movieA = MovieCreator.build(firstFile);
            movieB = MovieCreator.build(secondFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        final Movie finalMovie = new Movie();

        List<Track> audioTracks = new ArrayList<>();
        for (Movie movie : new Movie[] { movieA, movieB}) {
            for (Track track : movie.getTracks()) {
                if (track.getHandler().equals("soun")) {
                    audioTracks.add(track);
                }
            }
        }
        try {
            finalMovie.addTrack(new AppendTrack(audioTracks.toArray(new Track[audioTracks.size()])));
            final Container container = new DefaultMp4Builder().build(finalMovie);
             final String extStoragePath = Environment.getExternalStorageDirectory().getAbsolutePath();
             String filePath = extStoragePath + "/myAudio3.3gp";
            File mergedFile = new File(filePath);
            final FileOutputStream fos = new FileOutputStream(mergedFile);
            FileChannel fc = new RandomAccessFile(mergedFile, "rw").getChannel();
            container.writeContainer(fc);
            fc.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

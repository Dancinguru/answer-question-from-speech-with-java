package com.pro.speech.utils;

import ai.rev.speechtotext.ApiClient;
import ai.rev.speechtotext.RevAiWebSocketListener;
import ai.rev.speechtotext.StreamingClient;
import ai.rev.speechtotext.models.streaming.SessionConfig;
import ai.rev.speechtotext.models.streaming.StreamContentType;
import ai.rev.speechtotext.models.asynchronous.RevAiCaptionType;
import ai.rev.speechtotext.models.asynchronous.RevAiJob;
import ai.rev.speechtotext.models.asynchronous.RevAiJobStatus;
import ai.rev.speechtotext.models.asynchronous.RevAiTranscript;
import ai.rev.speechtotext.models.streaming.ConnectedMessage;
import ai.rev.speechtotext.models.streaming.Hypothesis;
import okhttp3.Response;
import okio.ByteString;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.apache.maven.wagon.observers.Debug;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class RevAiStreaming {

  private static String accessToken = "02izLg6RIJaXIE7uaMyyxJfTzWYaKKrzQWvBas2ugbgZ2ypjAmyyC1XX46tJ20sb52vIFavlTs9nPROD0nrATywo0eQ4c";
  static String res_id;
  public static String streamFromLocalFile(String filePath) throws InterruptedException, IOException {

    // Configure the streaming content type
    StreamContentType streamContentType = new StreamContentType();
    streamContentType.setContentType("audio/x-raw"); // audio content type
    streamContentType.setLayout("interleaved"); // layout
    streamContentType.setFormat("S16LE"); // format
    streamContentType.setRate(16000); // sample rate
    streamContentType.setChannels(1); // channels

    // Setup the SessionConfig with any optional parameters
    SessionConfig sessionConfig = new SessionConfig();
    sessionConfig.setMetaData("Streaming from the Java SDK");
    sessionConfig.setFilterProfanity(true);

    // Initialize your client with your access token
    StreamingClient streamingClient = new StreamingClient(accessToken);

    // Initialize your WebSocket listener
    WebSocketListener webSocketListener = new WebSocketListener();

    // Begin the streaming session
    streamingClient.connect(webSocketListener, streamContentType, sessionConfig);

    // Read file from disk


    // Convert file into byte array
    // byte[] fileByteArray = new byte[(int) file.length()];
    // try (final FileInputStream fileInputStream = new FileInputStream(file)) {
    //   BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
    //   try (final DataInputStream dataInputStream = new DataInputStream(bufferedInputStream)) {
    //     dataInputStream.readFully(fileByteArray, 0, fileByteArray.length);
    //   } catch (IOException e) {
    //     throw new RuntimeException(e.getMessage());
    //   }
    // } catch (IOException e) {
    //   throw new RuntimeException(e.getMessage());
    // }
    ApiClient apiClient = new ApiClient(accessToken);
    File file = new ClassPathResource(
      filePath).getFile();
    FileInputStream fileInputStream;
    try {
      fileInputStream = new FileInputStream(file);
    } catch (FileNotFoundException e) {
      throw new RuntimeException("Could not find file [" + file.getName() + "]");
    }
    RevAiJob revAiJob = apiClient.submitJobLocalFile(fileInputStream, "1.mp3");// check job status
    
    String jobId = revAiJob.getJobId();
    System.out.println("Job Id: " + jobId);
    System.out.println("Job Status: " + revAiJob.getJobStatus());
    System.out.println("Created On: " + revAiJob.getCreatedOn());

    // Waits 5 seconds between each status check to see if job is complete
    boolean isJobComplete = false;
    while (!isJobComplete) {
      RevAiJob retrievedJob;
      try {
        retrievedJob = apiClient.getJobDetails(jobId);
      } catch (IOException e) {
        throw new RuntimeException("Failed to retrieve job [" + jobId + "] " + e.getMessage());
      }

      RevAiJobStatus retrievedJobStatus = retrievedJob.getJobStatus();
      if (retrievedJobStatus == RevAiJobStatus.TRANSCRIBED
          || retrievedJobStatus == RevAiJobStatus.FAILED) {
        isJobComplete = true;
      } else {
        try {
          Thread.sleep(5000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }

    // Get the transcript and caption outputs
    RevAiTranscript objectTranscript;
    String textTranscript = "";
    InputStream srtCaptions;
    InputStream vttCaptions;

    try {
      objectTranscript = apiClient.getTranscriptObject(jobId);
      textTranscript = apiClient.getTranscriptText(jobId);
      srtCaptions = apiClient.getCaptions(jobId, RevAiCaptionType.SRT);
      vttCaptions = apiClient.getCaptions(jobId, RevAiCaptionType.VTT);
    } catch (IOException e) {
      e.printStackTrace();
    }

    System.out.println(textTranscript);
    /*
     * The job can now be deleted. Deleting the job will remove ALL information
     * about the job from the Rev AI servers. Subsequent requests to Rev AI that
     * use the deleted jobs Id will return 404's.
     */
    apiClient.deleteJob(jobId);
    return textTranscript;
  }

  // Your WebSocket listener for all streaming responses
  private static class WebSocketListener implements RevAiWebSocketListener {

    @Override
    public void onConnected(ConnectedMessage message) {
      System.out.println("********************* This is connected *********************");
      res_id = message.getId();
      System.out.println(message);
      System.out.println(message);
      
    }

    @Override
    public void onHypothesis(Hypothesis hypothesis) {
      System.out.println("********************* This is Hypothesis *********************");
      System.out.println(hypothesis.toString());
    }

    @Override
    public void onError(Throwable t, Response response) {
      System.out.println("********************* This is Error *********************");
      System.out.println(response);
    }

    @Override
    public void onClose(int code, String reason) {

      System.out.println("********************* This is close *********************");
      System.out.println(reason);
    }

    @Override
    public void onOpen(Response response) {

      System.out.println("********************* This is open *********************");
      
      System.out.println(response.toString());
    }
  }
}

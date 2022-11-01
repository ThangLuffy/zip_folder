package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping(path = "/api")
public class DownloadZipController {
    private static final Logger logger = LoggerFactory.getLogger(DownloadZipController.class);


    @GetMapping(path = "/downloads/large-files/{sampleId}")
    public ResponseEntity<StreamingResponseBody> downloadZip(HttpServletResponse response,
                                                             @PathVariable(name = "sampleId") String sampleId) {

        logger.info("download request for sampleId = {}", sampleId);

        // list of file paths for download
        List<String> paths = Arrays.asList("/home/Videos/part1.mp4",
                "/home/Videos/part2.mp4",
                "/home/Videos/part3.mp4",
                "/home/Videos/part4.pp4");

        int BUFFER_SIZE = 1024;

        StreamingResponseBody streamResponseBody = out -> {

            final ZipOutputStream zipOutputStream = new ZipOutputStream(response.getOutputStream());
            ZipEntry zipEntry = null;
            InputStream inputStream = null;

            try {
                for (String path : paths) {
                    File file = new File(path);
                    zipEntry = new ZipEntry(file.getName());

                    inputStream = new FileInputStream(file);

                    zipOutputStream.putNextEntry(zipEntry);
                    byte[] bytes = new byte[BUFFER_SIZE];
                    int length;
                    while ((length = inputStream.read(bytes)) >= 0) {
                        zipOutputStream.write(bytes, 0, length);
                    }

                }
                // set zip size in response
                response.setContentLength((int) (zipEntry != null ? zipEntry.getSize() : 0));
            } catch (IOException e) {
                logger.error("Exception while reading and streaming data {} ", e);
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (zipOutputStream != null) {
                    zipOutputStream.close();
                }
            }

        };

        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=example.zip");
        response.addHeader("Pragma", "no-cache");
        response.addHeader("Expires", "0");

        return ResponseEntity.ok(streamResponseBody);
    }
}

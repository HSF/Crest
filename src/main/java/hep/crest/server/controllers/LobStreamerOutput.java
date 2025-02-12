package hep.crest.server.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Abstract class for StreamingOutput.
 */
@Slf4j
public abstract class LobStreamerOutput implements StreamingOutput {
    /**
     * The transaction manager.
     */
    private final PlatformTransactionManager transactionManager;

    /**
     * @param transactionManager
     * The ctor.
     */
    protected LobStreamerOutput(
                        PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    /**
     * @return InputStream.
     */
    public abstract InputStream getInputStream();

    @Override
    public void write(OutputStream output) throws WebApplicationException {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setReadOnly(true);
        Integer nProcessed = transactionTemplate.execute(status -> {
            AtomicInteger processed = new AtomicInteger();
            try (InputStream inputStream = getInputStream()) {
                int read = 0;
                final byte[] bytes = new byte[2048];
                // Read input bytes and write in output stream
                while ((read = inputStream.read(bytes)) != -1) {
                    output.write(bytes, 0, read);
                    processed.getAndAdd(read);
                    log.trace("Copying {} bytes into the output...", read);
                }
                // Flush data
                output.flush();
            }
            catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            return processed.get();
        });
        log.info("Processed {} bytes", nProcessed);
    }
}

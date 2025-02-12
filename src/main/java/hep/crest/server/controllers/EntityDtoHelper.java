package hep.crest.server.controllers;

import hep.crest.server.converters.GenericMapper;
import org.springframework.context.ApplicationContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import jakarta.ws.rs.core.StreamingOutput;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Helper class to transform list of entities in list of DTOs.
 *
 * @author formica
 *
 */
@Component
public class EntityDtoHelper {

    /**
     * Mapper.
     */
    @Autowired
    private ApplicationContext applicationContext;
    /**
     * The transaction manager.
     */
    @Autowired
    private PlatformTransactionManager transactionManager;

    /**
     * @param <D>
     *            the destination class
     * @param <T>
     *            the entity source class
     * @param entitylist
     *            List of T
     * @param clazz
     *            the D class to which you to convert.
     * @param mapperClass
     * @return List<D>
     */
    public <D, T> List<D> entityToDtoList(List<T> entitylist, Class<D> clazz,
                                          Class<? extends GenericMapper<D, T>> mapperClass) {
        GenericMapper<D, T> mapper = applicationContext.getBean(mapperClass);
        return StreamSupport.stream(entitylist.spliterator(), false).map(mapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * @param <D>
     *            the destination class
     * @param <T>
     *            the entity source class
     * @param entitylist
     *            Iterable of T
     * @param clazz
     *            the D class to which you to convert.
     * @param mapperClass
     * @return List<D>
     */
    public <D, T> List<D> entityToDtoList(Iterable<T> entitylist, Class<D> clazz,
                                          Class<? extends GenericMapper<D, T>> mapperClass) {
        GenericMapper<D, T> mapper = applicationContext.getBean(mapperClass);
        return StreamSupport.stream(entitylist.spliterator(), false).map(mapper::toDto)
                .collect(Collectors.toList());
    }


    /**
     * Create a StreamingOutput using the provider in input.
     * @param streamProvider
     * @return StreamingOutput
     */
    public StreamingOutput makeStreamingOutputFromLob(LobStreamerProvider streamProvider) {
        return new LobStreamerOutput(transactionManager) {
            public InputStream getInputStream() {
                return streamProvider.getInputStream();
            }
        };
    }

}

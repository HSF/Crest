package hep.crest.server.data.runinfo.pojo;

import lombok.Data;
import lombok.experimental.Accessors;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.math.BigInteger;

@Embeddable
@Data
@Accessors(chain = true)
public class RunLumiId implements Serializable {
    /**
     * The run number.
     */
    @Column(name = "RUN", nullable = false, precision = 38, scale = 0)
    private BigInteger runNumber;
    /**
     * The lumi block.
     */
    @Column(name = "LUMI_BLOCK", nullable = false, precision = 38, scale = 0)
    private BigInteger lb;
}

package com.acme.sica.domain.port;

import com.acme.sica.domain.model.BitacoraAuditoria;
import java.util.List;

public interface AuditRepository {
    void save(BitacoraAuditoria log);
    List<BitacoraAuditoria> findAllRecent(int limit);
}

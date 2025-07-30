package ma.khairy.t_enant.modules;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InstalledModuleRepository extends JpaRepository<InstalledModule, String> {
}
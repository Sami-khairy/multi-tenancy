package ma.khairy.t_enant.modules;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "installed_module")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InstalledModule {

    @Id
    private String moduleName;

    // Colonne ajoutée pour le statut d'activation
    private boolean active;
}
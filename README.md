Application CRUD Multi-Tenant avec Spring Boot et PostgreSQL
Ce projet est une démonstration d'une application CRUD (Create, Read, Update, Delete) construite avec Spring Boot, utilisant une architecture multi-tenant robuste basée sur la stratégie "un schéma par tenant" avec une base de données PostgreSQL.

L'objectif principal est de fournir une isolation complète des données entre les différents tenants tout en maintenant une base de code unique et propre. L'application est conçue pour être résiliente, avec une gestion des erreurs claire qui distingue les erreurs client (ex: tenant non trouvé) des erreurs de configuration serveur.

🚀 Concepts Clés : Stratégie "Schema-per-Tenant"
La stratégie "un schéma par tenant" consiste à allouer un schéma de base de données distinct pour chaque client (tenant). Toutes les tables sont dupliquées dans chaque schéma, mais les données sont complètement isolées.

Avantages :

Forte Isolation des Données : La meilleure isolation après la stratégie "une base de données par tenant".

Simplicité de Sauvegarde/Restauration : Il est facile de sauvegarder ou de restaurer les données d'un tenant spécifique.

Flexibilité du Schéma : Permet potentiellement d'avoir des schémas de table différents pour certains tenants.

Inconvénients :

Complexité des Migrations : Mettre à jour le schéma de la base de données nécessite d'appliquer les changements à chaque schéma de chaque tenant.

🏗️ Architecture Détaillée
Le fonctionnement de la solution repose sur une chaîne de composants qui collaborent pour identifier le tenant à chaque requête HTTP et configurer la connexion à la base de données en conséquence.

1. Interception de la Requête HTTP (TenantInterceptor)
À chaque requête entrante, le TenantInterceptor (un HandlerInterceptor de Spring MVC) inspecte les en-têtes HTTP.

Il recherche l'en-tête X-TenantID.

Si l'en-tête est trouvé, sa valeur est stockée dans le TenantContext.

Crucial : Après la fin de la requête (dans la méthode afterCompletion), l'intercepteur nettoie le contexte pour éviter toute fuite de données vers la requête suivante traitée par le même thread.

2. Contexte du Tenant (TenantContext)
Cette classe utilise un ThreadLocal<String> pour stocker l'ID du tenant.

Un ThreadLocal garantit que la valeur stockée est accessible uniquement par le thread qui traite la requête actuelle, assurant ainsi une isolation parfaite entre les requêtes concurrentes.

3. Intégration avec Hibernate
L'intégration avec Hibernate est configurée via une classe HibernateConfig qui fournit des beans gérés par Spring, évitant ainsi les problèmes d'instanciation directe par Hibernate.

a. Résolution de l'ID du Tenant (TenantIdentifierResolver)
Cette classe implémente l'interface CurrentTenantIdentifierResolver d'Hibernate.

Son unique rôle est de lire la valeur stockée dans le TenantContext et de la fournir à Hibernate lorsque celui-ci a besoin de connaître le tenant actuel.

b. Fourniture de la Connexion (SchemaMultiTenantConnectionProvider)
C'est le cœur de la logique multi-tenant. Cette classe implémente MultiTenantConnectionProvider<String>.

Obtention d'une connexion : Elle récupère une connexion du pool de connexions (HikariCP).

Vérification de l'Existence du Schéma : Avant toute chose, elle exécute une requête de vérification sur les méta-données de PostgreSQL :

SELECT 1 FROM information_schema.schemata WHERE schema_name = ?

Si cette requête ne retourne aucun résultat, cela signifie que le schéma n'existe pas. Une exception personnalisée TenantNotFoundException est alors lancée. C'est la seule méthode fiable pour détecter un tenant inexistant, car SET SCHEMA seul ne lève pas d'erreur dans PostgreSQL pour un schéma non existant.

Changement de Schéma : Si la vérification réussit, la commande suivante est exécutée sur la connexion :

SET SCHEMA 'nom_du_tenant';

Toutes les requêtes SQL ultérieures effectuées sur cette connexion s'exécuteront dans le contexte du schéma du tenant.

Libération de la Connexion : Lorsque la connexion est retournée au pool, elle est réinitialisée au schéma par défaut (public) pour garantir qu'elle est "propre" pour la prochaine utilisation.

🐘 Configuration de la Base de Données (PostgreSQL)
Pour que l'application fonctionne, la base de données doit être préparée.

Création de la Base de Données : Assurez-vous d'avoir une base de données et un utilisateur dédiés sur votre serveur PostgreSQL.

Création des Schémas et Tables : Ce projet utilise une approche "database-first" où les scripts SQL sont la source de vérité pour la structure de la base de données.

src/main/resources/schema.sql : Contient les instructions CREATE SCHEMA et CREATE TABLE pour tous les tenants. Il doit être maintenu manuellement.

src/main/resources/data.sql : Contient les instructions INSERT pour peupler la base avec des données de test.

Configuration Spring Boot : Le fichier application.properties est configuré pour que Spring Boot exécute ces scripts au démarrage :

# On désactive la gestion automatique par Hibernate pour prendre le contrôle total.
spring.jpa.hibernate.ddl-auto=none

# On force l'exécution des scripts SQL, même pour une base de données non embarquée.
spring.sql.init.mode=always

🛡️ Gestion Robuste des Erreurs
Une attention particulière a été portée à la gestion des erreurs pour fournir des retours clairs et exploitables. Un GlobalExceptionHandler (@ControllerAdvice) intercepte les exceptions et les transforme en réponses HTTP structurées.

Scénario d'Erreur

Cause

Exception Interceptée

Réponse HTTP

Signification pour le Client

Tenant Invalide

L'en-tête X-TenantID contient une valeur pour un schéma qui n'existe pas (ex: tenant_xyz).

TenantNotFoundException

404 Not Found

"Le tenant que vous demandez n'existe pas."

Tenant Mal Configuré

Le schéma du tenant existe, mais une table requise (ex: produit) est manquante. C'est une erreur de déploiement/configuration.

InvalidDataAccessResourceUsageException

500 Internal Server Error

"Votre requête est valide, mais une erreur de configuration est survenue sur le serveur."

Cette distinction est essentielle pour un débogage efficace.

⚙️ Comment Lancer et Tester
Prérequis
Java 17+

Maven 3.6+

Un serveur PostgreSQL en cours d'exécution.

Lancement
Configurez la base de données dans src/main/resources/application.properties.

Créez les schémas et tables en exécutant le contenu de schema.sql sur votre base de données.

Lancez l'application avec la commande Maven :

mvn spring-boot:run

Tester avec curl
1. Requête réussie (Tenant A)
curl -i -X GET http://localhost:8080/produits -H "X-TenantID: tenant_a"

2. Tenant non trouvé (404 Not Found)
curl -i -X GET http://localhost:8080/produits -H "X-TenantID: tenant_inexistant"

Réponse attendue :

{
    "timestamp": "...",
    "status": 404,
    "error": "Not Found",
    "message": "Tenant 'tenant_inexistant' non trouvé.",
    "path": "/produits"
}

3. Tenant mal configuré (500 Internal Server Error)
Pour simuler ce cas, connectez-vous à votre base de données et supprimez la table produit d'un tenant existant :

DROP TABLE tenant_b.produit;

Puis, lancez la requête :

curl -i -X GET http://localhost:8080/produits -H "X-TenantID: tenant_b"

Réponse attendue :

{
    "timestamp": "...",
    "status": 500,
    "error": "Internal Server Error",
    "message": "Erreur de configuration du serveur : une table requise est manquante pour le tenant actuel.",
    "..."
}

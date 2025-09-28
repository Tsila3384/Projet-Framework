# Projet Framework - URL Monitor

## Description
Projet servlet Jakarta EE qui permet d'intercepter et d'afficher toutes les URLs exécutées (excluant les fichiers statiques comme .jsp, .html, .css, .js).

## Fonctionnalités
- **Filter servlet Jakarta EE** : Intercepte toutes les requêtes HTTP
- **Logger d'URLs** : Enregistre les URLs avec timestamp
- **Interface web** : Affiche la liste des URLs interceptées
- **Script de déploiement** : Génère le JAR et le copie dans le classpath de Tomcat

## Structure du projet
```
Projet-Framework/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/framework/
│       │       ├── filter/
│       │       │   └── URLMonitorFilter.java
│       │       └── servlet/
│       │           ├── URLDisplayServlet.java
│       │           └── URLAPIServlet.java
│       ├── webapp/
│       │   ├── WEB-INF/
│       │   │   └── web.xml
│       │   └── index.jsp
├── pom.xml
├── deploy.bat
├── deploy.ps1
├── test-monitor.html
└── setup-interface.bat
```

## Installation et déploiement

### Méthode 1 - PowerShell (recommandée)
```powershell
# Dans PowerShell, utiliser l'un des commandes suivantes :
.\deploy.bat        # Script batch depuis PowerShell
# ou
.\deploy.ps1        # Script PowerShell natif
```

### Méthode 2 - Invite de commandes (cmd)
```cmd
deploy.bat          # Script batch classique
```

### Étapes du déploiement
1. Ouvrir PowerShell dans le dossier du projet
2. Exécuter : `.\deploy.bat` 
3. Le script va :
   - Nettoyer et compiler le projet Maven
   - Générer le fichier JAR
   - Arrêter Tomcat
   - Copier le JAR dans `CATALINA_HOME/lib`
   - Redémarrer Tomcat

## Configuration Tomcat
- **Tomcat 10+** requis (Jakarta EE)
- Port par défaut : 8080
- Déploiement : JAR copié dans `CATALINA_HOME/lib`
- Classes disponibles pour toutes les applications web

## Dépannage
- **Erreur "deploy.bat n'est pas reconnu"** : Utilisez `.\deploy.bat` dans PowerShell
- **Maven non trouvé** : Vérifiez que Maven est dans le PATH
- **Erreur Tomcat** : Vérifiez le chemin CATALINA_HOME dans le script
- **Incompatibilité Jakarta** : Utilisez Tomcat 10+ (pas Tomcat 9)

## Lancement et test de l'application

### 1. Déploiement initial
```powershell
# Dans PowerShell, depuis le dossier du projet
.\deploy.bat
```

### 2. Vérification de Tomcat
- Attendez que Tomcat démarre complètement (environ 10-15 secondes)
- Vérifiez les logs dans `%CATALINA_HOME%/logs/catalina.out`
- Le JAR `url-monitor.jar` doit être présent dans `%CATALINA_HOME%/lib/`

### 3. Test du monitoring
Le JAR contient un Filter qui s'active automatiquement sur **toutes les applications** déployées sur Tomcat.

#### Option A : Créer une application de test simple
Créez un dossier `test-app` dans `%CATALINA_HOME%/webapps/` avec :

**webapps/test-app/index.html :**
```html
<!DOCTYPE html>
<html>
<head><title>Test App</title></head>
<body>
    <h1>Application de Test</h1>
    <p><a href="/test-app/servlet1">Test Servlet 1</a></p>
    <p><a href="/test-app/api/users">Test API</a></p>
    <p><a href="/test-app/admin/dashboard">Test Admin</a></p>
</body>
</html>
```

#### Option B : Utiliser les applications Tomcat existantes
```
http://localhost:8080/manager/
http://localhost:8080/examples/
```

### 4. Génération de trafic et visualisation
1. **Naviguez vers différentes URLs :**
   ```
   http://localhost:8080/test-app/
   http://localhost:8080/test-app/servlet1
   http://localhost:8080/test-app/api/users  
   http://localhost:8080/examples/servlets/
   ```

2. **Consultez les URLs interceptées :**
   - Le Filter capture automatiquement toutes les requêtes
   - Les URLs sont stockées en mémoire dans `URLMonitorFilter`
   - **Problème :** Pas d'interface web dans le JAR seul

### 5. Visualisation des données (Solutions)

#### Solution A : Logs dans la console Tomcat
Modifiez `URLMonitorFilter.java` pour ajouter des logs :
```java
// Dans doFilter(), après urlHistory.offer(entry);
System.out.println("[URL-MONITOR] " + method + " " + requestURI + " - " + timestamp);
```

#### Solution B : Créer une application web séparée
Créez `webapps/monitor/` avec un servlet qui lit `URLMonitorFilter.getURLHistory()` :

**webapps/monitor/WEB-INF/web.xml :**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app version="4.0">
    <servlet>
        <servlet-name>MonitorServlet</servlet-name>
        <servlet-class>com.framework.servlet.URLDisplayServlet</servlet-class>
    </servlet>
    <servlet-mapping>
        <servlet-name>MonitorServlet</servlet-name>
        <url-pattern>/</url-pattern>
    </servlet-mapping>
</web-app>
```

**webapps/monitor/index.jsp :** (copier depuis src/main/webapp/monitor.jsp)

#### Solution C : API REST pour récupérer les données
Ajoutez un servlet API dans le JAR qui retourne du JSON :
```java
@WebServlet("/api/urls")
public class URLAPIServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        response.setContentType("application/json");
        // Retourner URLMonitorFilter.getURLHistory() en JSON
    }
}
```

### 6. Test complet recommandé

1. **Déployer le JAR :**
   ```powershell
   .\deploy.bat
   ```

2. **Créer l'app de test :**
   - Créer `webapps/test-app/index.html`
   - Créer `webapps/monitor/` pour visualiser

3. **Générer du trafic :**
   ```
   http://localhost:8080/test-app/page1
   http://localhost:8080/test-app/api/data
   http://localhost:8080/examples/servlets/servlet/HelloWorldExample
   ```

4. **Vérifier les captures :**
   - Consulter logs Tomcat pour les System.out.println
   - Accéder à `http://localhost:8080/monitor/` si créé

### 7. URLs utiles pour les tests
```
# Applications Tomcat par défaut
http://localhost:8080/
http://localhost:8080/manager/html
http://localhost:8080/examples/

# Vos URLs de test  
http://localhost:8080/test-app/
http://localhost:8080/test-app/servlet1
http://localhost:8080/test-app/api/users

# Interface monitoring (si créée)
http://localhost:8080/monitor/
```

**Note :** Le JAR seul ne fournit pas d'interface web. Il faut soit créer une application séparée, soit ajouter des logs pour voir les captures.

## Comment voir les URLs interceptées

### Méthode 1 : Logs dans la console Tomcat (Immédiat)
Après le déploiement avec `.\deploy.bat`, les URLs interceptées s'affichent dans la console Tomcat :
```
[URL-MONITOR] 2024-01-15 14:30:25 | GET | /test-app/servlet1
[URL-MONITOR] 2024-01-15 14:30:28 | POST | /api/users/123
```

**Pour voir les logs :**
- Console Tomcat (si lancé manuellement)
- Fichier `%CATALINA_HOME%/logs/catalina.out` (Linux/Mac)
- Fichier `%CATALINA_HOME%/logs/catalina.YYYY-MM-DD.log` (Windows)

### Méthode 2 : API JSON (Recommandée)
Le JAR expose une API REST accessible depuis n'importe quelle application :

```
GET http://localhost:8080/api/monitor/urls
```

**Réponse JSON :**
```json
{
  "status": "success",
  "total": 5,
  "urls": [
    {
      "timestamp": "2024-01-15 14:30:25",
      "method": "GET",
      "url": "/test-app/servlet1"
    }
  ]
}
```

### Méthode 3 : Interface web (test-monitor.html)

#### Comment accéder à l'interface web :

**Option 1 - Fichier local (Rapide)**
1. Ouvrir le fichier `test-monitor.html` directement dans votre navigateur
2. Double-cliquer sur `test-monitor.html` 
3. **OU** clic droit → "Ouvrir avec" → votre navigateur
4. L'interface se charge à `file:///E:/ITU/L3/S5/Mr%20Naina/Projet-Framework/test-monitor.html`

**Option 2 - Via Tomcat (Recommandée)**
1. Copier `test-monitor.html` dans le dossier webapps de Tomcat :
   ```
   copy test-monitor.html "E:\ITU\L2\S4\Examen-Web-dynamique\apache-tomcat-10.1.28\webapps\ROOT\monitor.html"
   ```
2. Accéder à : `http://localhost:8080/monitor.html`

**Option 3 - Application dédiée**
1. Créer le dossier : `%CATALINA_HOME%\webapps\monitor\`
2. Copier `test-monitor.html` vers `%CATALINA_HOME%\webapps\monitor\index.html`
3. Accéder à : `http://localhost:8080/monitor/`

#### Utilisation de l'interface :
1. **Actualiser** : Charge les URLs interceptées depuis l'API
2. **Auto-refresh** : Active/désactive l'actualisation automatique (5 secondes)
3. **Vider** : Vide l'affichage local (pas les données serveur)
4. **Table** : Affiche timestamp, méthode HTTP, et URL

#### Dépannage interface web :
- **Erreur CORS** : Utilisez l'Option 2 (via Tomcat) au lieu du fichier local
- **API non accessible** : Vérifiez que Tomcat est démarré et le JAR déployé
- **Pas de données** : Générez du trafic en naviguant sur d'autres pages
- **Caractères bizarres** : Problème d'encodage UTF-8 - utilisez la version corrigée
- **"Erreur de chargement"** : L'API `/api/monitor/urls` n'est pas accessible

#### Diagnostic des problèmes courants :
1. **L'API ne répond pas :**
   - Vérifiez que Tomcat est démarré : `http://localhost:8080/`
   - Vérifiez que le JAR est présent dans `CATALINA_HOME/lib/url-monitor.jar`
   - Redéployez avec `.\deploy.bat`

2. **"HTTP 404" sur l'API :**
   - Le servlet `URLAPIServlet` n'était pas créé - maintenant ajouté
   - Redéployez le projet avec `.\deploy.bat`
   - Vérifiez les logs Tomcat pour les erreurs de déploiement

3. **Aucune donnée dans l'interface :**
   - Générez du trafic en visitant d'autres pages Tomcat
   - L'API fonctionne mais aucune URL n'a été interceptée

#### SOLUTION RAPIDE pour Jakarta EE :
```powershell
# 1. Redéployer le JAR avec Jakarta EE
.\deploy.bat

# 2. Vérifier la version Tomcat (doit être 10+)
# Tomcat 9 = Java EE (javax.*)
# Tomcat 10+ = Jakarta EE (jakarta.*)

# 3. Tester l'API directement dans le navigateur
# http://localhost:8080/api/monitor/urls

# 4. Générer du trafic
# http://localhost:8080/
# http://localhost:8080/examples/

# 5. Re-tester l'interface
```

### Versions compatibles :
- **Tomcat 10+** : Jakarta EE (jakarta.*)
- **Tomcat 9** : Java EE (javax.*)
- **Projet actuel** : Jakarta EE compatible

### Remarques finales
- Ce projet est conçu pour Tomcat 10+ avec Jakarta EE.
- Assurez-vous que votre environnement utilise les bonnes versions de Tomcat et Java.
- Pour toute question, consulter la documentation officielle de Tomcat et Jakarta EE.

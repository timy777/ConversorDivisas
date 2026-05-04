# 🚀 Guía de Despliegue en Azure DevOps y Azure App Service

## 📋 Requisitos Previos

1. **Cuenta de Azure** - [Crear una cuenta gratuita](https://azure.microsoft.com/es-es/free/)
2. **Azure DevOps Project** - [Crear organización](https://dev.azure.com)
3. **Repositorio GitHub** - Código subido
4. **Permiso de administrador** en Azure DevOps

---

## 🔧 Paso 1: Crear Azure App Service

### Crear Resource Group y App Service

```bash
# Variables
RESOURCE_GROUP="rg-diplomado"
APP_NAME="conversor-divisas"
LOCATION="eastus"

# Crear resource group
az group create \
  --name $RESOURCE_GROUP \
  --location $LOCATION

# Crear App Service Plan (Linux, B1 gratuito)
az appservice plan create \
  --name "${APP_NAME}-plan" \
  --resource-group $RESOURCE_GROUP \
  --sku B1 \
  --is-linux

# Crear Web App para Java 17
az webapp create \
  --resource-group $RESOURCE_GROUP \
  --plan "${APP_NAME}-plan" \
  --name $APP_NAME \
  --runtime "JAVA|17-java17"
```

---

## 🔗 Paso 2: Conectar GitHub con Azure DevOps

### 2.1 En Azure DevOps

1. Ir a `https://dev.azure.com/{organization}`
2. Crear nuevo **Project**
3. Nombre: `ConversorDivisas`
4. Visibilidad: **Private** (recomendado)
5. Crear

### 2.2 Conectar Repositorio GitHub

1. En el proyecto, ir a **Repos** → **Files**
2. Hacer clic en **Import a repository**
3. Seleccionar **Git**
4. URL: `https://github.com/timy777/ConversorDivisas.git`
5. Importar

---

## 📦 Paso 3: Crear el Pipeline en Azure DevOps

### 3.1 Crear Pipeline

1. Ir a **Pipelines** → **Pipelines**
2. Hacer clic en **New pipeline**
3. Seleccionar **GitHub YAML**
4. Autorizar GitHub si es necesario
5. Seleccionar repositorio: `timy777/ConversorDivisas`

### 3.2 Configurar el Pipeline

1. En **Review your pipeline YAML**, pasar a código
2. Copiar contenido de `azure-pipelines.yml` local
3. Hacer clic en **Save and run**

---

## 🔐 Paso 4: Configurar Variables y Conexiones

### 4.1 Crear Service Connection a Azure

1. En proyecto, ir a **Project Settings** → **Service connections**
2. Nueva conexión: **Azure Resource Manager**
3. Authentication method: **Service principal (automatic)**
4. Seleccionar subscription y grupo de recursos
5. Nombre: `azure-subscription-conn`

### 4.2 Agregar Variables al Pipeline

1. En el pipeline, hacer clic en **Edit**
2. Agregar variables en `azure-pipelines.yml`:

```yaml
variables:
  AZURE_SUBSCRIPTION: 'azure-subscription-conn'
  AZURE_APP_NAME: 'conversor-divisas'
  AZURE_RESOURCE_GROUP: 'rg-diplomado'
```

---

## 🧪 Paso 5: Ejecutar el Pipeline

### 5.1 Ejecutar Manualmente

1. En **Pipelines**, seleccionar el pipeline
2. Hacer clic en **Run**
3. Seleccionar rama: `main`
4. Hacer clic en **Run**

### 5.2 Monitorear Ejecución

```
BUILD ✅ - Compilación exitosa
TEST ✅ - 17 tests pasados
PACKAGE ✅ - JAR generado
DEPLOY ✅ - Desplegado a Azure
```

---

## 🌐 Paso 6: Acceder a la Aplicación

Una vez desplegada:

```
URL: https://conversor-divisas.azurewebsites.net

Endpoints:
- Health: https://conversor-divisas.azurewebsites.net/api/v1/health
- Monedas: https://conversor-divisas.azurewebsites.net/api/v1/monedas
- Convertir: https://conversor-divisas.azurewebsites.net/api/v1/convertir/USD/EUR/100
```

---

## 📊 Configuración Avanzada

### Monitoreo y Logging

```bash
# Ver logs en tiempo real
az webapp log tail \
  --name conversor-divisas \
  --resource-group rg-diplomado

# Ver métricas
az monitor metrics list \
  --resource-group rg-diplomado \
  --resource-type "Microsoft.Web/sites" \
  --resource "conversor-divisas"
```

### Configurar Variables de Entorno

```bash
az webapp config appsettings set \
  --resource-group rg-diplomado \
  --name conversor-divisas \
  --settings \
    SPRING_PROFILES_ACTIVE=production \
    SERVER_PORT=8080
```

---

## 🔄 Configurar Deployments Automáticos

### Trigger en cada Push

En `azure-pipelines.yml`, configurar triggers:

```yaml
trigger:
  - main        # Deploy en cambios a main
  - develop     # Deploy en cambios a develop

pr:
  - main        # Ejecutar tests en Pull Requests
```

### Configurar Stages Condicionales

```yaml
stages:
  - stage: Deploy
    condition: eq(variables['Build.SourceBranch'], 'refs/heads/main')
    # Solo deploy en branch main
```

---

## 🐛 Troubleshooting

### Error: "release version 17 not supported"

**Solución**: Verificar versión de Maven compiler plugin en `pom.xml`:

```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-compiler-plugin</artifactId>
  <version>3.11.0</version>
  <configuration>
    <source>17</source>
    <target>17</target>
  </configuration>
</plugin>
```

### Error: "Failed to authenticate"

**Solución**: Verificar token de GitHub en Azure DevOps:
1. Project Settings → Service connections
2. Editar conexión GitHub
3. Autorizar nuevamente

### La app no inicia

**Solución**: Verificar logs:

```bash
az webapp log tail --name conversor-divisas --resource-group rg-diplomado
```

---

## 📝 Checklist Final

- [ ] Repositorio GitHub conectado
- [ ] Azure App Service creado
- [ ] Pipeline YAML configurado
- [ ] Service Connection a Azure creada
- [ ] Variables de ambiente configuradas
- [ ] Tests ejecutados exitosamente
- [ ] Aplicación desplegada en Azure
- [ ] Endpoints funcionando correctamente
- [ ] Logs disponibles en Azure

---

## 📚 Recursos Útiles

- [Documentación Azure DevOps](https://docs.microsoft.com/es-es/azure/devops/)
- [Maven + Spring Boot en Azure](https://docs.microsoft.com/es-es/azure/app-service/quickstart-java)
- [Azure CLI Reference](https://docs.microsoft.com/es-es/cli/azure/)
- [GitHub Actions Alternative](https://docs.github.com/es/actions)

---

¿Necesitas ayuda adicional? Contacta al equipo de DevOps.

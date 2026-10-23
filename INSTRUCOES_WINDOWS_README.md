# Eletric Calculator - Projeto Android Completo

Aplicativo Android profissional para dimensionamento elétrico, cálculo de queda de tensão (NBR 5410), conversão de potência e corrente (Lei de Ohm: P = V × I), e cálculo de preços de cabos elétricos flexíveis com tabelas semanais de fornecedores.

---

## 🛠️ Tecnologias Utilizadas

- **Linguagem:** Kotlin 2.2+
- **Interface de Usuário:** Jetpack Compose com Material Design 3 (M3)
- **Persistência de Dados Local:** SQLite gerenciado via Jetpack Room Database (`cabocalc_database.db`) e `SharedPreferences` para preferências e autenticação
- **Arquitetura:** MVVM (Model-View-ViewModel) com StateFlow e Coroutines
- **Build System:** Gradle (Kotlin DSL - `.gradle.kts`) com Version Catalog (`gradle/libs.versions.toml`)
- **Compatibilidade:** Android 7.0 (API 24 - Min SDK) até Android 16 (API 36 - Target SDK)

---

## 💻 Qual Programa Usar para Abrir no Windows?

O programa oficial e recomendado para abrir, editar e executar este projeto no Windows é o:

### **Android Studio**
- **Download gratuito oficial:** https://developer.android.com/studio
- Versão recomendada: **Android Studio Ladybug (2024.2+)** ou superior.
- O instalador do Android Studio já inclui automaticamente o JDK (Java Development Kit), o SDK do Android e o emulador integrado.

---

## 🚀 Passo a Passo para Abrir e Executar no Windows

1. **Baixe e Instale o Android Studio:**
   - Acesse o link acima, baixe o instalador `.exe` para Windows e conclua a instalação padrão (deixando marcadas as opções de Android SDK e Android Virtual Device).

2. **Descompacte o Projeto:**
   - Extraia o conteúdo deste arquivo ZIP em uma pasta do seu computador (exemplo: `C:\Projetos\EletricCalculator`).
   - *Dica:* Evite caminhos com caracteres especiais ou espaços muito longos.

3. **Abrir o Projeto no Android Studio:**
   - Abra o **Android Studio**.
   - Na tela inicial, clique em **Open** (ou no menu superior: `File -> Open...`).
   - Navegue até a pasta onde você descompactou o projeto e selecione a pasta raiz (ela tem o ícone do robô Android ou do Gradle).
   - Clique em **OK**.

4. **Sincronização Automática do Gradle:**
   - O Android Studio fará o download automático das dependências declaradas em `gradle/libs.versions.toml` e no `build.gradle.kts`.
   - Aguarde o término da barra de sincronização inferior (*"Gradle Sync finished"*).

5. **Executar o Aplicativo:**
   - Para testar em um **Emulador**: no menu superior do Android Studio, abra o *Device Manager*, crie um dispositivo virtual (ex: Pixel 8) e inicie-o.
   - Para testar no **seu celular físico**: ative a *"Depuração USB"* nas Opções de Desenvolvedor do seu celular Android, conecte o cabo USB no computador e selecione seu celular na lista de dispositivos.
   - Clique no botão verde de **Run** (ícone de Play ▶️ ou atalho `Shift + F10`). O aplicativo será compilado e instalado imediatamente.

---

## 🗄️ Onde os Dados são Armazenados?

1. **Tabelas Semanais e Orçamentos da Obra:**
   - São armazenados localmente no banco de dados SQLite nativo do dispositivo, gerenciado pelo **Room**:
     `data/data/com.aistudio.cabocalc.qrvw/databases/cabocalc_database.db`
2. **Dados de Sessão e Tutorial:**
   - São armazenados no arquivo interno protegido:
     `data/data/com.aistudio.cabocalc.qrvw/shared_prefs/eletric_calc_auth.xml`

### 🔄 Os dados podem ser compartilhados entre o Android e o Computador?
- **Atualmente:** Como o armazenamento é local e protegido dentro do dispositivo (*sandbox* do Android), os dados cadastrados no celular (como novos orçamentos ou preços editados) ficam salvos localmente no aparelho. Se você rodar o projeto no emulador do Windows, ele terá seu próprio banco de dados local independente.
- **Como compartilhar orçamentos hoje:** Você pode usar a função nativa do aplicativo **"Compartilhar Orçamento (WhatsApp / Copiar Texto)"**, que exporta a lista completa formatada com valores, metragens e pesos de cobre.
- **Para sincronização em tempo real na nuvem no futuro:** É possível conectar o aplicativo a um banco de dados em nuvem (como Firebase Firestore), permitindo que qualquer alteração feita no celular apareça simultaneamente no computador e vice-versa.

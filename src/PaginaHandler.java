import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Serve o HTML completo da interface supervisória via rota /.
 * RNF-01: Dark Mode  |  RNF-07: Funciona em qualquer navegador moderno
 */
public class PaginaHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange req) throws IOException {
        String html = gerarHTML();
        req.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
        byte[] bytes = html.getBytes("UTF-8");
        req.sendResponseHeaders(200, bytes.length);
        OutputStream out = req.getResponseBody();
        out.write(bytes);
        out.close();
    }

    private String gerarHTML() {
        return cabecalho() + corpo() + scripts() + rodape();
    }

    // ── Cabecalho com <head> e abertura do <body> ──
    private String cabecalho() {
        return """
<!DOCTYPE html>
<html lang="pt-BR">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Sistema de Monitoramento Integrado — SENAI</title>
<style>
/* Estilos adicionados no proximo commit */
* { margin:0; padding:0; box-sizing:border-box; }
body { font-family:'Segoe UI',system-ui,sans-serif; background:#0d1117; color:#e6edf3; }
</style>
</head>
<body>
""";
    }

    // ── Corpo HTML com topbar, sidebar e paginas ──
    private String corpo() {
        return topbar() + sidebar() + conteudo();
    }

    private String topbar() {
        return """
<header class="topbar">
  <div class="topbar-left">
    <button onclick="toggleSidebar()" class="btn-menu">&#9776;</button>
    <div class="brand">
      <span>⚙</span>
      <div>
        <div class="brand-title">Sistema de Monitoramento Integrado</div>
        <span class="brand-sub">SENAI — Célula Robótica</span>
      </div>
    </div>
  </div>
  <button class="btn-emergencia" id="btnEmergencia" onclick="acionarEmergencia()">⬛ PARADA EMERGÊNCIA</button>
  <div class="topbar-right">
    <div class="relogio" id="relogio">00:00:00</div>
    <div class="notif-wrapper" onclick="toggleNotif()">
      🔔<span class="notif-badge" id="badgeNotif">3</span>
      <div class="notif-dropdown" id="dropNotif">
        <div class="notif-header">Notificações</div>
        <div class="notif-item aviso">⚠ Tempo de ciclo acima do limite</div>
        <div class="notif-item info">ℹ Câmera CAM-01 reconectada</div>
        <div class="notif-item ok">✓ Backup de logs realizado</div>
      </div>
    </div>
    <div class="usuario" onclick="toggleUserMenu()">
      <div class="avatar">V</div><span>Vitor</span>
      <div class="user-menu" id="userMenu">
        <div class="user-menu-item" onclick="ir('perfil')">👤 Perfil</div>
        <div class="user-menu-item sair" onclick="sair()">🚪 Sair</div>
      </div>
    </div>
  </div>
</header>
""";
    }

    private String sidebar() {
        return """
<aside class="sidebar" id="sidebar">
  <nav class="sidebar-nav">
    <div class="sidebar-item ativo" onclick="ir('dashboard')" data-pag="dashboard">
      <span>📊</span><span class="sidebar-label">Painel Geral</span>
    </div>
    <div class="sidebar-item" onclick="ir('perfil')" data-pag="perfil">
      <span>👤</span><span class="sidebar-label">Perfil e Cadastro</span>
    </div>
    <div class="sidebar-item" onclick="ir('configuracoes')" data-pag="configuracoes">
      <span>⚙</span><span class="sidebar-label">Configurações</span>
    </div>
    <div class="sidebar-item" onclick="ir('logs')" data-pag="logs">
      <span>📋</span><span class="sidebar-label">Logs Completos</span>
    </div>
  </nav>
  <div class="sidebar-versao">v1.0.0 — SENAI</div>
</aside>
""";
    }

    private String conteudo() {
        return "<main class=\"conteudo\" id=\"conteudo\">"
            + pagDashboard() + pagPerfil() + pagConfig() + pagLogs()
            + "</main>"
            + overlayEmergencia();
    }

    private String pagDashboard() {
        return """
<div class="pagina ativa" id="pag-dashboard">
  <div class="barra-status">
    <div class="status-card"><div class="indicador azul" id="indRobo"></div>
      <div><div class="status-label">Status do Robô (RF-02)</div>
      <div class="status-valor" id="statusRobo">Rampa 3 em execução</div></div></div>
    <div class="status-card"><div>
      <div class="status-label">Programa (RF-03)</div>
      <div><span class="badge verde" id="badgePrograma">Habilitado</span></div></div></div>
    <div class="status-card"><div>
      <div class="status-label">Comunicação (RF-04)</div>
      <div><span class="badge verde" id="badgeComm">Online</span></div></div></div>
  </div>
  <div class="metricas-grid">
    <div class="metrica-card"><div class="metrica-label">Peças Processadas (RF-05)</div>
      <div class="metrica-valor" id="pecasTotal">1.257</div><div class="metrica-un">peças</div></div>
    <div class="metrica-card"><div class="metrica-label">Tempo de Ciclo (RF-06)</div>
      <div class="metrica-valor laranja" id="tempoCiclo">4.2</div><div class="metrica-un">seg/peça</div></div>
    <div class="metrica-card"><div class="metrica-label">Taxa de Acerto (RF-07)</div>
      <div class="metrica-valor verde" id="taxaAcerto">98.5</div><div class="metrica-un">%</div></div>
    <div class="metrica-card"><div class="metrica-label">OEE (RF-12)</div>
      <div class="metrica-valor azul" id="oeeVal">85.0</div><div class="metrica-un">%</div></div>
    <div class="metrica-card"><div class="metrica-label">Disponib. Mecânica (RF-13)</div>
      <div class="metrica-valor azul" id="dispMec">92.3</div><div class="metrica-un">%</div></div>
  </div>
  <div class="grid-inferior">
    <div class="painel">
      <div class="painel-header">📹 CAM-01 — Esquemático da Célula
        <span class="cam-ok" id="statusCam">🟢 Processando</span></div>
      <div class="camera-view">
        <div class="cam-placeholder"><div class="cam-spin"></div>
          <span style="font-size:12px;margin-top:6px;">Feed ao vivo — CAM-01</span></div>
        <div class="rec-badge"><div class="rec-ponto"></div> REC</div>
      </div>
    </div>
    <div class="painel">
      <div class="painel-header">🎨 Distribuição de Peças por Cor (RF-14)</div>
      <div style="padding:14px;">
        <div class="peca-linha"><div class="peca-cor" style="background:#e53935;"></div>
          <div class="peca-nm">Vermelhas</div>
          <div class="barra-wrap"><div class="barra" id="bVerm" style="width:42%;background:#e53935;"></div></div>
          <div class="peca-qt" id="qVerm">528</div></div>
        <div class="peca-linha"><div class="peca-cor" style="background:#43a047;"></div>
          <div class="peca-nm">Verdes</div>
          <div class="barra-wrap"><div class="barra" id="bVerd" style="width:35%;background:#43a047;"></div></div>
          <div class="peca-qt" id="qVerd">440</div></div>
        <div class="peca-linha"><div class="peca-cor" style="background:#1e88e5;"></div>
          <div class="peca-nm">Azuis</div>
          <div class="barra-wrap"><div class="barra" id="bAzul" style="width:23%;background:#1e88e5;"></div></div>
          <div class="peca-qt" id="qAzul">289</div></div>
        <div style="padding-top:8px;font-size:13px;color:#8b949e;">Total: <strong id="totPecas">1.257</strong></div>
      </div>
    </div>
    <div class="painel">
      <div class="painel-header">📋 LOGS DO ROBÔ (RF-11)</div>
      <div class="logs-lista" id="logsLista"></div>
    </div>
  </div>
</div>
""";
    }

    private String pagPerfil() {
        return """
<div class="pagina" id="pag-perfil">
  <div class="pag-titulo">👤 Perfil e Cadastro</div>
  <div class="form-card">
    <div class="form-linha"><label>Nome</label>
      <input class="form-input" type="text" value="Vitor Apolinário"></div>
    <div class="form-linha"><label>E-mail</label>
      <input class="form-input" type="email" value="vitorapolinariob1@gmail.com"></div>
    <div class="form-linha"><label>Perfil (RNF-10)</label>
      <select class="form-input"><option>Operador</option><option selected>Técnico</option><option>Administrador</option></select></div>
    <button class="btn-salvar">Salvar Alterações</button>
  </div>
</div>
""";
    }

    private String pagConfig() {
        return """
<div class="pagina" id="pag-configuracoes">
  <div class="pag-titulo">⚙ Configurações</div>
  <div class="form-card">
    <div class="form-linha"><label>Endpoint WebSocket (RNF-03)</label>
      <input class="form-input" type="text" value="ws://192.168.1.100:8765"></div>
    <div class="form-linha"><label>MQTT Broker</label>
      <input class="form-input" type="text" value="mqtt://192.168.1.100:1883"></div>
    <div class="form-linha"><label>Latência máx. vídeo ms (RNF-02)</label>
      <input class="form-input" type="number" value="200"></div>
    <div class="form-linha"><label>Intervalo atualização ms (RNF-03)</label>
      <input class="form-input" type="number" value="100"></div>
    <button class="btn-salvar">Salvar</button>
  </div>
</div>
""";
    }

    private String pagLogs() {
        return """
<div class="pagina" id="pag-logs">
  <div class="pag-titulo">📋 Logs Completos (RNF-09)</div>
  <div class="logs-completo" id="logsCompleto"></div>
</div>
""";
    }

    private String overlayEmergencia() {
        return """
<div class="overlay-em" id="overlayEm">
  <div class="modal-em">
    <div style="font-size:60px;">🛑</div>
    <h2>PARADA DE EMERGÊNCIA ACIONADA</h2>
    <p>Resposta ao CLP em <strong id="tempoMs">— ms</strong></p>
    <button class="btn-liberar" onclick="liberarEmergencia()">Liberar Sistema</button>
  </div>
</div>
""";
    }

    // ── Scripts JavaScript (adicionados no proximo commit) ──
    private String scripts() { return ""; }

    private String rodape() { return "</body></html>"; }
}

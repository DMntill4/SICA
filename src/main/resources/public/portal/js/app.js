// Portal de Autoservicio de Visitantes SICA - Lógica de Selección de Caminos, Biometría Facial e Inserción Inteligente

let selectedRoad = 'NUEVO'; // 'FRECUENTE' | 'NUEVO'
let currentStep = 0; // 0: Road Select, 1: Datos, 2: Modalidad, 3: Biometría 5s, 'user-hub': Hub Usuario Frecuente, 4: Pase Emitido
let selectedCategory = 'NEGOCIOS';
let currentVector = null;
let currentFotoRealBase64 = null;
let lastGeneratedPassId = null;
let authenticatedUser = null; // Guardar datos de usuario autenticado por rostro
let isCreatingVisitForAuthenticatedUser = false;

document.addEventListener('DOMContentLoaded', () => {

    updateWizardUI();
});

// SISTEMA DE NOTIFICACIONES Y CONFIRMACIONES CUSTOM HTML/CSS (SIN USAR BROWSER ALERTS NATIVOS)
function mostrarNotificacionCustomSica(titulo, mensaje, icono = '🔒') {
    return new Promise((resolve) => {
        const overlay = document.getElementById('sica-custom-modal-overlay');
        const iconDiv = document.getElementById('sica-modal-icon');
        const titleEl = document.getElementById('sica-modal-title');
        const msgEl = document.getElementById('sica-modal-message');
        const btnConfirm = document.getElementById('sica-modal-btn-confirm');
        const btnCancel = document.getElementById('sica-modal-btn-cancel');

        if (!overlay) {
            resolve(true);
            return;
        }

        iconDiv.innerText = icono;
        titleEl.innerText = titulo || "Notificación SICA";
        msgEl.innerText = mensaje || "";
        btnCancel.style.display = 'none';
        btnConfirm.innerText = "Aceptar";
        overlay.style.display = 'flex';

        btnConfirm.onclick = () => {
            overlay.style.display = 'none';
            resolve(true);
        };
    });
}

function mostrarConfirmacionCustomSica(titulo, mensaje, icono = '❓') {
    return new Promise((resolve) => {
        const overlay = document.getElementById('sica-custom-modal-overlay');
        const iconDiv = document.getElementById('sica-modal-icon');
        const titleEl = document.getElementById('sica-modal-title');
        const msgEl = document.getElementById('sica-modal-message');
        const btnConfirm = document.getElementById('sica-modal-btn-confirm');
        const btnCancel = document.getElementById('sica-modal-btn-cancel');

        if (!overlay) {
            resolve(true);
            return;
        }

        iconDiv.innerText = icono;
        titleEl.innerText = titulo || "Confirmación Requerida";
        msgEl.innerText = mensaje || "";
        btnCancel.style.display = 'inline-block';
        btnConfirm.innerText = "Confirmar";
        btnCancel.innerText = "Cancelar";
        overlay.style.display = 'flex';

        btnConfirm.onclick = () => {
            overlay.style.display = 'none';
            resolve(true);
        };

        btnCancel.onclick = () => {
            overlay.style.display = 'none';
            resolve(false);
        };
    });
}

function selectRoad(road) {
    selectedRoad = road;
    isCreatingVisitForAuthenticatedUser = false;
    
    if (road === 'FRECUENTE') {

        // ROAD A: Directo a escaneo biométrico (Paso 3)
        currentStep = 3;
        const scanTitle = document.getElementById('scan-step-title');
        const scanDesc = document.getElementById('scan-step-desc');
        if (scanTitle) scanTitle.innerText = "Road A: Escáner Facial (Autenticación de Usuario Frecuente)";
        if (scanDesc) scanDesc.innerText = "Centra tu rostro frente a la cámara. El escáner de 5 segundos validará tu firma facial para ingresar a tu Panel de Gestión.";

        if (window.iniciarCamaraIA) {
            window.iniciarCamaraIA();
        }
    } else {
        // ROAD B: Registro completo paso a paso (Paso 1)
        currentStep = 1;
        const scanTitle = document.getElementById('scan-step-title');
        const scanDesc = document.getElementById('scan-step-desc');
        if (scanTitle) scanTitle.innerText = "Road B: Verificación Biométrica Facial Obligatoria (5 Segundos)";
        if (scanDesc) scanDesc.innerText = "Realiza el escaneo de 5 segundos para vincular tu firma biométrica a tu registro de nuevo visitante.";
    }

    updateWizardUI();
}

async function goToStep(step) {
    // Validar Paso 1 en Road B antes de avanzar
    if (step === 2 && currentStep === 1) {
        const nombre = document.getElementById('nombreCompleto').value.trim();
        const doc = document.getElementById('docIdentidad').value.trim();
        const email = document.getElementById('email').value.trim();
        const empresa = document.getElementById('empresaDestino').value.trim();

        if (!nombre || !doc || !email || !empresa) {
            await mostrarNotificacionCustomSica(
                "PASO 1 REQUERIDO",
                "Por favor completa tus Datos Personales (*): Nombre, Documento, Correo y Empresa Destino antes de continuar.",
                "⚠️"
            );
            return;
        }

        // VERIFICACIÓN DE SEGURIDAD: NO PERMITIR RE-REGISTRAR UN DOCUMENTO YA EXISTENTE
        try {
            const checkRes = await fetch(`/api/biometria/verificar-doc?doc=${encodeURIComponent(doc)}`);
            if (checkRes.ok) {
                const docData = await checkRes.json();
                if (docData.existe) {
                    // Si el usuario ya está autenticado por rostro con ese documento, permitir continuar limpiamente
                    if (authenticatedUser && String(authenticatedUser.docIdentidad).trim() === String(doc).trim()) {
                        // Usuario autenticado creando una nueva visita para sí mismo -> Continuar
                    } else {
                        await mostrarNotificacionCustomSica(
                            "CONTROL DE ACCESO Y SEGURIDAD SICA",
                            `El documento N° [${doc}] YA se encuentra registrado a nombre de ${docData.nombreCompleto}.\n\nPor políticas de seguridad, no puedes registrar un nuevo perfil con este documento. Debes ingresar mediante Verificación Biométrica Facial (Road A - Usuario Frecuente).\n\nSe te redirigirá a la cámara web para validar tu identidad.`,
                            "🔒"
                        );
                        
                        selectRoad('FRECUENTE');
                        return;
                    }
                }
            }
        } catch (e) {
            console.warn("No se pudo verificar el documento:", e);
        }
    }

    if (step === 3) {
        if (window.iniciarCamaraIA) {
            window.iniciarCamaraIA();
        }
    }

    currentStep = step;
    updateWizardUI();
}

function updateWizardUI() {
    const roadScreen = document.getElementById('road-select-screen');
    if (roadScreen) {
        roadScreen.classList.toggle('active', currentStep === 0);
    }

    const sections = [1, 2, 3, 4, 'user-hub'];
    sections.forEach(secId => {
        const sec = document.getElementById(`step-section-${secId}`);
        if (sec) {
            sec.classList.toggle('active', secId === currentStep || String(secId) === String(currentStep));
        }
    });

    window.scrollTo({ top: 0, behavior: 'smooth' });
}

function selectCategory(cat, el) {
    selectedCategory = cat;
    document.querySelectorAll('.category-card').forEach(c => c.classList.remove('active'));
    el.classList.add('active');

    const txtMotivo = document.getElementById('motivo');
    if (cat === 'EMERGENCIA') {
        txtMotivo.value = 'Soporte Técnico e Infraestructura Crítica';
    } else if (cat === 'EXPRESS') {
        txtMotivo.value = 'Entrega de Paquetería / Correspondencia';
    } else if (cat === 'ESTADIA_PROLONGADA') {
        txtMotivo.value = 'Mantenimiento / Proyecto Temporal';
    } else {
        txtMotivo.value = 'Desarrollo de Software y Reunión Técnica';
    }
}

function iniciarSecuenciaEscaneo5s() {
    const progressBox = document.getElementById('scan-progress-box');
    const progressFill = document.getElementById('progress-bar-fill');
    const stepMsg = document.getElementById('scan-step-msg');
    const countdownText = document.getElementById('scan-countdown-text');
    const statusText = document.getElementById('ai-status-text');
    const statusDot = document.getElementById('status-dot');
    const btnScan = document.getElementById('btn-scan-face');
    const btnSubmit = document.getElementById('btn-submit-pass');
    const banner = document.getElementById('autofill-banner');

    progressBox.style.display = 'block';
    btnScan.disabled = true;
    btnScan.style.opacity = '0.5';

    if (window.iniciarEscaneoReal5Segundos) {
        window.iniciarEscaneoReal5Segundos(
            (data) => {
                progressFill.style.width = data.progress + '%';
                countdownText.innerText = data.secondsLeft + 's';
                stepMsg.innerText = data.message;
                statusText.innerText = `[🤖] Escaneando rostro real (${data.progress}%): ${data.message}`;
            },
            async (result) => {
                btnScan.disabled = false;
                btnScan.style.opacity = '1.0';
                if (statusDot) statusDot.className = 'pulse-dot green';

                currentFotoRealBase64 = result.fotoBase64;
                currentVector = result.vectorBiometrico;

                statusText.innerText = "[✓] Escaneo Facial de 5 Segundos Completado Exitosamente";

                // INTERCEPCIÓN DE SEGURIDAD BIOMÉTRICA: VERIFICAR SI EL ROSTRO YA EXISTE EN BD
                let matchedPersona = null;
                try {
                    const bioRes = await fetch('/api/biometria', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({
                            vectorBiometrico: JSON.stringify(currentVector)
                        })
                    });


                    if (bioRes.ok) {
                        const bioData = await bioRes.json();
                        if (bioData.coincidencia) {
                            matchedPersona = bioData;
                        }
                    }
                } catch (e) {
                    console.warn("Error en verificación biométrica:", e);
                }

                // CASO A: USUARIO FRECUENTE AUTENTICADO QUE ESTÁ EMITIENDO UNA NUEVA VISITA
                if (isCreatingVisitForAuthenticatedUser && authenticatedUser) {
                    isCreatingVisitForAuthenticatedUser = false;
                    banner.style.display = 'flex';
                    if (btnSubmit) btnSubmit.style.display = 'inline-block';
                    setTimeout(() => {
                        procesarSolicitudPaseInteligente();
                    }, 500);

                // CASO B: EL ROSTRO COINCIDE CON UN REGISTRO EN BASE DE DATOS (NUEVO O LOGIN)
                } else if (matchedPersona) {
                    if (selectedRoad === 'NUEVO') {
                        // INTERCEPCIÓN DE SEGURIDAD: Intentó registrarse como nuevo con otro nombre, pero su rostro ya existe!
                        await mostrarNotificacionCustomSica(
                            "DETECCIÓN DE IDENTIDAD BIOMÉTRICA SICA",
                            `¡Atención! Tu rostro ya está registrado en el sistema a nombre de:\n\n👤 ${matchedPersona.nombreCompleto}\n📄 Documento N° ${matchedPersona.docIdentidad}\n\nPor políticas de seguridad, no puedes registrar cuentas duplicadas. Se ha autenticado tu perfil real y serás redirigido a tu Panel de Usuario.`,
                            "🔒"
                        );
                    }

                    authenticatedUser = matchedPersona;
                    cargarPerfilHubUsuario();

                    setTimeout(() => {
                        goToStep('user-hub');
                    }, 400);


                } else if (selectedRoad === 'FRECUENTE') {
                    // ROSTRO NO RECONOCIDO EN CAMINO DE USUARIO FRECUENTE -> RECHAZO Y REORIENTACIÓN
                    await mostrarNotificacionCustomSica(
                        "ACCESO DENEGADO - ROSTRO NO REGISTRADO",
                        "Tu rostro no coincide con ningún usuario registrado en la base de datos SICA.\n\nPor favor regístrate como Nuevo Visitante (Road B) para crear tu perfil.",
                        "⛔"
                    );
                    selectRoad('NUEVO');

                } else {
                    // ROAD B (NUEVO VISITANTE AUTÉNTICO) -> CONTINUAR REGISTRO
                    banner.style.display = 'flex';
                    if (btnSubmit) btnSubmit.style.display = 'inline-block';
                    setTimeout(() => {
                        procesarSolicitudPaseInteligente();
                    }, 500);
                }
            }
        );
    }
}

// Carga los datos del perfil y estado de acceso en el Hub de Usuario Frecuente
function cargarPerfilHubUsuario() {
    if (!authenticatedUser) return;

    document.getElementById('hub-user-name').innerText = authenticatedUser.nombreCompleto || 'Visitante Registrado';
    document.getElementById('hub-user-doc').innerText = `Documento: ${authenticatedUser.docIdentidad}`;
    
    const hubPhoto = document.getElementById('hub-user-photo');
    const hubPlaceholder = document.getElementById('hub-user-placeholder');
    if (hubPhoto && currentFotoRealBase64) {
        hubPhoto.src = currentFotoRealBase64;
        hubPhoto.style.display = 'block';
        if (hubPlaceholder) hubPlaceholder.style.display = 'none';
    }

    // ACTUALIZACIÓN DE ESTADO DE ACCESO (HABILITADO / RESTRINGIDO)
    const accessBadge = document.getElementById('hub-access-badge');
    const restrictionBanner = document.getElementById('hub-restriction-banner');
    const btnNewVisit = document.getElementById('btn-new-visit-frecuente');

    const estado = (authenticatedUser.estadoAcceso || 'HABILITADO').toUpperCase();

    if (estado === 'RESTRINGIDO') {
        if (accessBadge) {
            accessBadge.className = 'cat-tag red';
            accessBadge.innerText = 'ESTADO: RESTRINGIDO';
        }
        if (restrictionBanner) restrictionBanner.style.display = 'block';
        if (btnNewVisit) {
            btnNewVisit.disabled = true;
            btnNewVisit.style.opacity = '0.5';
            btnNewVisit.style.cursor = 'not-allowed';
            btnNewVisit.title = "Cuenta restringida. No se permite solicitar nuevos pases.";
        }
    } else {
        if (accessBadge) {
            accessBadge.className = 'cat-tag green';
            accessBadge.innerText = 'ESTADO: HABILITADO';
        }
        if (restrictionBanner) restrictionBanner.style.display = 'none';
        if (btnNewVisit) {
            btnNewVisit.disabled = false;
            btnNewVisit.style.opacity = '1.0';
            btnNewVisit.style.cursor = 'pointer';
            btnNewVisit.title = "";
        }
    }

    verVisitasPendientesUsuario();
}

// 1. GESTIÓN FRECUENTE: VER HISTORIAL Y ESTADO EN VIVO DE VISITAS (SINCRONIZADO CON PORTERÍA)
async function verVisitasPendientesUsuario() {

    if (!authenticatedUser || !authenticatedUser.docIdentidad) return;

    const docUser = String(authenticatedUser.docIdentidad || '').trim();
    const listContainer = document.getElementById('user-visits-list-container');
    const listTitle = document.getElementById('user-visits-list-title');
    const kpiBadge = document.getElementById('user-visits-kpi-badge');
    const cardsDiv = document.getElementById('user-visits-cards');

    cardsDiv.innerHTML = "<p style='color: var(--text-muted); font-size: 13px;'>🔄 Obteniendo estado actualizado de visitas en vivo desde portería...</p>";
    listContainer.style.display = 'block';
    listTitle.innerText = "📋 Historial y Estado en Vivo de Visitas (Sincronizado con Portería)";

    try {
        const [resPersona, resPases, resVisitas] = await Promise.all([
            fetch(`/api/personas?doc=${docUser}`).then(r => r.ok ? r.json() : null).catch(() => null),
            fetch(`/api/pases/persona/${docUser}`).then(r => r.ok ? r.json() : []).catch(() => []),
            fetch(`/api/visitas`).then(r => r.ok ? r.json() : []).catch(() => [])
        ]);

        // 1. VERIFICACIÓN DE RESTRICCIÓN O BLOQUEO DE SEGURIDAD (PERSONA)
        let estadoPersona = 'HABILITADO';
        if (resPersona && resPersona.estadoAcceso) {
            estadoPersona = resPersona.estadoAcceso;
        } else if (authenticatedUser.estadoAcceso) {
            estadoPersona = authenticatedUser.estadoAcceso;
        }

        const isRestringido = (estadoPersona === 'RESTRINGIDO');

        // Mostrar u ocultar banner de restricción en el Hub
        const restrBanner = document.getElementById('hub-restriction-banner');
        if (restrBanner) {
            restrBanner.style.display = isRestringido ? 'block' : 'none';
        }

        // 2. FILTRAR Y ORDENAR VISITAS DE LA BASE DE DATOS (NUEVA A VIEJA)
        const misVisitasDB = resVisitas.filter(v => 
            (v.personaDocIdentidad && String(v.personaDocIdentidad).trim() === docUser) ||
            (v.personaDoc && String(v.personaDoc).trim() === docUser) ||
            (v.personaId && (v.personaId == authenticatedUser.personaId || v.personaId == authenticatedUser.id))
        ).sort((a, b) => (b.id || 0) - (a.id || 0));

        if ((!resPases || resPases.length === 0) && (!misVisitasDB || misVisitasDB.length === 0)) {
            cardsDiv.innerHTML = "<p style='color: var(--text-muted); font-size: 13px;'>No tienes solicitudes ni visitas registradas en el sistema.</p>";
            if (kpiBadge) kpiBadge.innerText = "";
            return;
        }

        let items = [];

        // Mapear pases de la web
        resPases.forEach(p => {
            let estadoReal = p.estado;
            
            const ultVisita = misVisitasDB.length > 0 ? misVisitasDB[0] : null;

            if (isRestringido) {
                estadoReal = '🔴 ACCESO RESTRINGIDO POR SEGURIDAD SICA';
            } else if (ultVisita) {
                if (ultVisita.estadoVisita === 'DENTRO') estadoReal = '🟢 DENTRO (Ingreso Realizado en Portería)';
                else if (ultVisita.estadoVisita === 'FINALIZADO') estadoReal = '🏁 FINALIZADO (Salida Registrada)';
                else if (ultVisita.estadoVisita === 'APROBADO') estadoReal = '✅ APROBADO (Listo para Ingreso)';
                else if (ultVisita.estadoVisita === 'RECHAZADO') estadoReal = '🔴 RECHAZADO POR FUNCIONARIO';
                else if (ultVisita.estadoVisita === 'PENDIENTE_APROBACION_OLVIDO') estadoReal = '🪪 OLVIDO CARNET (Pendiente Aprobación)';
                else if (ultVisita.estadoVisita === 'PENDIENTE_APROBACION') estadoReal = '⏳ PENDIENTE DE APROBACIÓN POR FUNCIONARIO';
            }

            items.push({
                id: p.id,
                empresa: p.empresaDestino || 'Zona Acme',
                motivo: p.motivo,
                estado: estadoReal,
                rawEstado: p.estado
            });
        });

        // Agregar visitas directas de portería no contempladas en pases web
        misVisitasDB.forEach(v => {
            const yaMapeado = items.some(i => i.id === v.id || (i.motivo && v.motivo && i.motivo.includes(v.motivo)));
            if (!yaMapeado) {
                let estText = v.estadoVisita;
                if (isRestringido) estText = '🔴 ACCESO RESTRINGIDO POR SEGURIDAD SICA';
                else if (v.estadoVisita === 'DENTRO') estText = '🟢 DENTRO (Ingreso Realizado en Portería)';
                else if (v.estadoVisita === 'FINALIZADO') estText = '🏁 FINALIZADO (Salida Registrada)';
                else if (v.estadoVisita === 'APROBADO') estText = '✅ APROBADO (Listo para Ingreso)';
                else if (v.estadoVisita === 'PENDIENTE_APROBACION') estText = '⏳ PENDIENTE DE APROBACIÓN POR FUNCIONARIO';
                else if (v.estadoVisita === 'PENDIENTE_APROBACION_OLVIDO') estText = '🪪 OLVIDO CARNET (Pendiente Aprobación)';

                items.push({
                    id: v.id,
                    empresa: 'Zona Acme',
                    motivo: v.motivo || 'Registro en Portería',
                    estado: estText,
                    rawEstado: v.estadoVisita
                });
            }
        });

        const total = items.length;
        const dentroCount = items.filter(i => String(i.estado).includes('DENTRO')).length;
        const aprCount = items.filter(i => String(i.estado).includes('APROBADO')).length;

        if (kpiBadge) {
            kpiBadge.innerText = `Total: ${total} | Aprobadas: ${aprCount} | En Instalaciones: ${dentroCount}`;
        }

        cardsDiv.innerHTML = items.map(item => {
            let colorBorder = 'var(--border-color)';
            let badgeBg = 'rgba(59, 130, 246, 0.15)';
            let badgeColor = '#60A5FA';

            if (String(item.estado).includes('DENTRO')) {
                colorBorder = '#10B981';
                badgeBg = 'rgba(16, 185, 129, 0.2)';
                badgeColor = '#34D399';
            } else if (String(item.estado).includes('APROBADO')) {
                colorBorder = '#3B82F6';
                badgeBg = 'rgba(59, 130, 246, 0.2)';
                badgeColor = '#60A5FA';
            } else if (String(item.estado).includes('FINALIZADO')) {
                colorBorder = '#6B7280';
                badgeBg = 'rgba(107, 114, 128, 0.2)';
                badgeColor = '#9CA3AF';
            } else if (String(item.estado).includes('RESTRINGIDO') || String(item.estado).includes('RECHAZADO') || String(item.estado).includes('CANCELADO')) {
                colorBorder = '#EF4444';
                badgeBg = 'rgba(239, 68, 68, 0.2)';
                badgeColor = '#FCA5A5';
            }

            return `
                <div style="background: var(--bg-card); border: 1px solid ${colorBorder}; border-radius: 10px; padding: 14px; display: flex; justify-content: space-between; align-items: center; text-align: left;">
                    <div>
                        <div style="display: flex; gap: 8px; align-items: center;">
                            <strong style="color: var(--accent-primary); font-size: 14px;">Solicitud #${item.id}</strong>
                            <span style="font-size: 11px; padding: 3px 10px; border-radius: 12px; background: ${badgeBg}; color: ${badgeColor}; font-weight: 700;">
                                ${item.estado}
                            </span>
                        </div>
                        <div style="font-size: 13px; margin-top: 6px; color: var(--text-main); font-weight: 600;">${item.motivo}</div>
                        <div style="font-size: 12px; color: var(--text-muted); margin-top: 2px;">📍 Destino: ${item.empresa}</div>
                    </div>
                    ${(!isRestringido && (item.rawEstado === 'PENDIENTE_APROBACION' || item.rawEstado === 'APROBADO')) ? `
                        <button type="button" class="btn-danger" onclick="cancelarPasePorId(${item.id})" style="padding: 8px 12px; font-size: 12px; background: #EF4444; color: white; border: none; border-radius: 6px; cursor: pointer; font-weight: 600;">
                            🚫 Cancelar
                        </button>
                    ` : ''}
                </div>
            `;
        }).join('');

    } catch (e) {
        cardsDiv.innerHTML = "<p style='color: #EF4444; font-size: 13px;'>Error al obtener el historial de visitas.</p>";
    }
}

// CONSULTA DE ESTADO EN VIVO PARA TICKET DE PASE PRIMERA VEZ
async function actualizarEstadoTicketEnVivo() {
    const docElem = document.getElementById('t-doc');
    const badgeElem = document.getElementById('ticket-status-badge');
    if (!docElem || !badgeElem) return;

    const doc = docElem.innerText.trim();
    if (!doc) return;

    badgeElem.innerText = "🔄 CONSULTANDO...";
    badgeElem.style.background = "#3B82F6";

    try {
        const [resPersona, resPases, resVisitas] = await Promise.all([
            fetch(`/api/personas?doc=${doc}`).then(r => r.ok ? r.json() : null).catch(() => null),
            fetch(`/api/pases/persona/${doc}`).then(r => r.ok ? r.json() : []).catch(() => []),
            fetch(`/api/visitas`).then(r => r.ok ? r.json() : []).catch(() => [])
        ]);

        let estadoEncontrado = "PENDIENTE APROBACIÓN";
        let colorBg = "#F59E0B";

        if (resPersona && resPersona.estadoAcceso === 'RESTRINGIDO') {
            estadoEncontrado = "🔴 ACCESO RESTRINGIDO POR SEGURIDAD SICA";
            colorBg = "#EF4444";
        } else {
            const misVisitas = resVisitas.filter(v => 
                (v.personaDocIdentidad && String(v.personaDocIdentidad).trim() === doc) ||
                (v.personaDoc && String(v.personaDoc).trim() === doc)
            ).sort((a, b) => (b.id || 0) - (a.id || 0));

            if (misVisitas.length > 0) {
                const ultVisita = misVisitas[0];
                if (ultVisita.estadoVisita === 'DENTRO') {
                    estadoEncontrado = "🟢 DENTRO (Ingreso Realizado en Portería)";
                    colorBg = "#10B981";
                } else if (ultVisita.estadoVisita === 'FINALIZADO') {
                    estadoEncontrado = "🏁 FINALIZADO (Salida Registrada)";
                    colorBg = "#6B7280";
                } else if (ultVisita.estadoVisita === 'APROBADO') {
                    estadoEncontrado = "✅ APROBADO POR FUNCIONARIO";
                    colorBg = "#2563EB";
                } else if (ultVisita.estadoVisita === 'RECHAZADO') {
                    estadoEncontrado = "🔴 SOLICITUD RECHAZADA";
                    colorBg = "#EF4444";
                } else if (ultVisita.estadoVisita === 'PENDIENTE_APROBACION_OLVIDO') {
                    estadoEncontrado = "🪪 PENDIENTE APROBACIÓN POR OLVIDO CARNET";
                    colorBg = "#F59E0B";
                }
            } else if (resPases.length > 0) {
                const ultPase = resPases[resPases.length - 1];
                if (ultPase.estado === 'APROBADO') {
                    estadoEncontrado = "✅ APROBADO POR FUNCIONARIO";
                    colorBg = "#2563EB";
                } else if (ultPase.estado === 'RECHAZADO' || ultPase.estado === 'CANCELADO') {
                    estadoEncontrado = "🔴 RECHAZADO / CANCELADO";
                    colorBg = "#EF4444";
                }
            }
        }

        badgeElem.innerText = estadoEncontrado;
        badgeElem.style.background = colorBg;

        await mostrarNotificacionCustomSica(
            "ESTADO EN VIVO DESDE PORTERÍA",
            `🔄 El estado actual de tu solicitud en el sistema SICA es:\n\n👉 [ ${estadoEncontrado} ]`,
            "ℹ️"
        );

    } catch (e) {
        badgeElem.innerText = "ERROR AL CONSULTAR";
    }
}



// 2. GESTIÓN FRECUENTE: CANCELAR VISITAS ACTIVAS
async function abrirModalCancelarVisitaUsuario() {
    if (!authenticatedUser || !authenticatedUser.docIdentidad) return;

    const listContainer = document.getElementById('user-visits-list-container');
    const listTitle = document.getElementById('user-visits-list-title');
    const cardsDiv = document.getElementById('user-visits-cards');
    cardsDiv.innerHTML = "<p style='color: var(--text-muted); font-size: 13px;'>Cargando visitas para cancelación...</p>";
    listContainer.style.display = 'block';
    listTitle.innerText = "🚫 Cancelar / Anular Visita Registrada";

    try {
        const res = await fetch(`/api/pases/persona/${authenticatedUser.docIdentidad}`);
        if (res.ok) {
            const pases = await res.json();
            const activas = pases.filter(p => p.estado !== 'RECHAZADO' && p.estado !== 'CANCELADO');

            if (!activas || activas.length === 0) {
                cardsDiv.innerHTML = "<p style='color: var(--text-muted); font-size: 13px;'>No tienes visitas registradas para cancelar.</p>";
                return;
            }

            cardsDiv.innerHTML = activas.map(p => `
                <div style="background: var(--bg-card); border: 1px dashed #EF4444; border-radius: 8px; padding: 12px; display: flex; justify-content: space-between; align-items: center; text-align: left;">
                    <div>
                        <strong style="color: #EF4444; font-size: 14px;">Solicitud #${p.id}</strong> - <span style="font-size: 12px; color: var(--text-muted);">${p.empresaDestino || 'General'}</span>
                        <div style="font-size: 13px; margin-top: 4px; color: var(--text-main);">${p.motivo}</div>
                    </div>
                    <button type="button" class="btn-danger" onclick="cancelarPasePorId(${p.id})" style="padding: 10px 14px; font-size: 13px; background: #EF4444; color: white; border: none; border-radius: 6px; font-weight: 600; cursor: pointer;">
                        🚫 Confirmar Cancelación
                    </button>
                </div>
            `).join('');
        }
    } catch (e) {
        cardsDiv.innerHTML = "<p style='color: #EF4444; font-size: 13px;'>Error al cargar las visitas.</p>";
    }
}

async function cancelarPasePorId(id) {
    const confirmCancel = await mostrarConfirmacionCustomSica(
        "CONFIRMAR CANCELACIÓN",
        `🚫 ¿Estás seguro de cancelar la solicitud de visita #${id}?`,
        "🚫"
    );
    if (!confirmCancel) return;

    try {
        await fetch(`/api/pases/${id}/cancelar`, { method: 'POST' });
        await mostrarNotificacionCustomSica("VISITA CANCELADA", `✅ Solicitud de visita #${id} cancelada exitosamente.`, "✅");
        verVisitasPendientesUsuario();
    } catch (e) {
        await mostrarNotificacionCustomSica("ERROR", "Error al cancelar la visita: " + e.message, "❌");
    }
}

// 3. GESTIÓN FRECUENTE: REGISTRAR NUEVA VISITA (CAMPOS PRE-LLENADOS Y EDITABLES)
async function abrirFormularioNuevaVisitaFrecuente() {
    if (authenticatedUser && authenticatedUser.estadoAcceso === 'RESTRINGIDO') {
        await mostrarNotificacionCustomSica(
            "ACCESO RESTRINGIDO",
            "Tu cuenta tiene un bloqueo de seguridad preventivo. No tienes permitido registrar nuevas visitas. Acércate a la oficina de administración de accesos.",
            "🔴"
        );
        return;
    }

    if (authenticatedUser) {
        document.getElementById('nombreCompleto').value = authenticatedUser.nombreCompleto || '';
        document.getElementById('docIdentidad').value = authenticatedUser.docIdentidad || '';
        document.getElementById('email').value = authenticatedUser.email || '';
        document.getElementById('telefono').value = authenticatedUser.telefono || '';
        document.getElementById('empresaDestino').value = authenticatedUser.empresaNombre || 'General';
    }
    isCreatingVisitForAuthenticatedUser = true;
    currentStep = 1;
    updateWizardUI();
}



async function procesarSolicitudPaseInteligente() {
    if (!currentVector || !currentFotoRealBase64) {
        await mostrarNotificacionCustomSica(
            "ACCESO DENEGADO",
            "Debes presionar 'Iniciar Escaneo Facial (5s)' y esperar a que la cámara capture tu foto antes de enviar la solicitud.",
            "⚠️"
        );
        return;
    }

    let nombre = document.getElementById('nombreCompleto').value.trim();
    let doc = document.getElementById('docIdentidad').value.trim();
    let email = document.getElementById('email').value.trim();
    let tel = document.getElementById('telefono').value.trim();
    let empresa = document.getElementById('empresaDestino').value.trim();
    let motivo = document.getElementById('motivo').value.trim();

    if (!nombre) nombre = authenticatedUser ? authenticatedUser.nombreCompleto : "Visitante SICA";
    if (!doc) doc = authenticatedUser ? authenticatedUser.docIdentidad : "1010101010";
    if (!email) email = authenticatedUser ? authenticatedUser.email : "visitante@sica.local";
    if (!empresa) empresa = "General";
    if (!motivo) motivo = "Reunión de Trabajo";

    let finalMotivo = motivo;
    if (!finalMotivo.startsWith('[') && selectedCategory) {
        finalMotivo = `[${selectedCategory}] ${motivo}`;
    }

    const payload = {
        nombreCompleto: nombre,
        docIdentidad: doc,
        email: email,
        telefono: tel,
        empresaDestino: empresa,
        motivo: finalMotivo,
        vectorBiometrico: JSON.stringify(currentVector),
        fotoUrl: currentFotoRealBase64
    };

    try {
        const res = await fetch('/api/pases/solicitar', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (res.ok) {
            const passData = await res.json().catch(() => ({}));
            if (passData && passData.id) {
                lastGeneratedPassId = passData.id;
            }

            const elNombre = document.getElementById('t-nombre');
            if (elNombre) elNombre.innerText = nombre;

            const elDoc = document.getElementById('t-doc');
            if (elDoc) elDoc.innerText = doc;

            const elEmpresa = document.getElementById('t-empresa');
            if (elEmpresa) elEmpresa.innerText = empresa;

            const elMotivo = document.getElementById('t-motivo');
            if (elMotivo) elMotivo.innerText = finalMotivo;

            const elCat = document.getElementById('pass-category-label');
            if (elCat) elCat.innerText = `CATEGORÍA: ${selectedCategory}`;

            const photoImg = document.getElementById('pass-captured-photo');
            const placeholder = document.getElementById('pass-avatar-placeholder');
            if (photoImg && currentFotoRealBase64) {
                photoImg.src = currentFotoRealBase64;
                photoImg.style.display = 'block';
                if (placeholder) placeholder.style.display = 'none';
            }

            goToStep(4);

        } else {
            const errData = await res.json().catch(() => ({}));
            const errorMsg = errData.mensaje || errData.error || res.statusText || "Error en el servidor";
            await mostrarNotificacionCustomSica("ERROR EN SERVIDOR", "Error al procesar la solicitud de pase en el servidor:\n\n" + errorMsg, "❌");
        }

    } catch (err) {
        await mostrarNotificacionCustomSica("ERROR DE CONEXIÓN", "Error de conexión con el servidor SICA: " + err.message, "❌");
    }
}

async function cancelarVisitaActual() {
    const confirmCancel = await mostrarConfirmacionCustomSica(
        "CANCELAR VISITA",
        "🚫 ¿Estás seguro de cancelar esta solicitud de visita en SICA?",
        "🚫"
    );
    if (!confirmCancel) return;

    try {
        const passId = lastGeneratedPassId || 1;
        await fetch(`/api/pases/${passId}/cancelar`, { method: 'POST' });
    } catch (ignored) {}

    await mostrarNotificacionCustomSica("VISITA CANCELADA", "✅ Tu solicitud de visita ha sido CANCELADA exitosamente.", "✅");
    resetPortal();
}

// 4. REPORTAR NOVEDAD / PÉRDIDA DE CARNET ESTANDO ADENTRO
async function abrirModalReportarAnomaliaUsuario() {
    if (!authenticatedUser) {
        await mostrarNotificacionCustomSica("AUTENTICACIÓN REQUERIDA", "Debes estar autenticado por biometría facial para reportar una novedad de carnet.", "🔒");
        return;
    }

    const listContainer = document.getElementById('user-visits-list-container');
    const listTitle = document.getElementById('user-visits-list-title');
    const cardsDiv = document.getElementById('user-visits-cards');
    
    listContainer.style.display = 'block';
    listTitle.innerText = "🚨 Reportar Novedad / Pérdida de Carnet (Validado por Biometría IA)";

    cardsDiv.innerHTML = `
        <div style="background: var(--bg-card); border: 1px solid #F59E0B; border-radius: 10px; padding: 16px; text-align: left;">
            <div style="font-weight: 700; color: #F59E0B; font-size: 14px; margin-bottom: 8px;">
                👤 Reportante: ${authenticatedUser.nombreCompleto || 'Usuario Registrado'} (${authenticatedUser.docIdentidad})
            </div>
            
            <label style="display: block; font-size: 12.5px; color: var(--text-muted); margin-bottom: 6px; font-weight: 600;">Selecciona el tipo de Novedad / Incidencia (*):</label>
            <select id="select-tipo-anomalia" style="width: 100%; padding: 10px; border-radius: 6px; background: var(--bg-card-alt); color: var(--text-main); border: 1px solid var(--border-color); margin-bottom: 12px; font-size: 13px;">
                <option value="PERDIDA_CARNET">🪪 Pérdida / Extravío de Carnet Físico estando Adentro</option>
                <option value="OLVIDO_CARNET">🚪 Olvido de Carnet Físico / Requiero Pase Temporal de Salida</option>
                <option value="DIFICULTAD_TORNIQUETE">⚠️ Dificultad o Bloqueo de Ingreso/Salida en Punto de Acceso</option>
                <option value="ASISTENCIA_SEGURIDAD">🆘 Solicitar Asistencia Directa de Portería / Seguridad</option>
            </select>

            <label style="display: block; font-size: 12.5px; color: var(--text-muted); margin-bottom: 6px; font-weight: 600;">Detalles adicionales / Comentario (*):</label>
            <textarea id="txt-desc-anomalia" rows="3" placeholder="Ej: Se me extravió el carnet en el edificio A, solicito pase de salida temporal..." style="width: 100%; padding: 10px; border-radius: 6px; background: var(--bg-card-alt); color: var(--text-main); border: 1px solid var(--border-color); margin-bottom: 14px; font-size: 13px; resize: vertical;"></textarea>

            <button type="button" class="btn-neon" onclick="enviarReporteAnomaliaWeb()" style="width: 100%; padding: 12px; font-size: 14px; font-weight: 700; background: #F59E0B; color: #FFFFFF; border: none; border-radius: 8px; cursor: pointer;">
                🚨 Transmitir Reporte de Novedad con Firma Biométrica
            </button>
        </div>
    `;
}

async function enviarReporteAnomaliaWeb() {
    if (!authenticatedUser) return;

    const tipo = document.getElementById('select-tipo-anomalia').value;
    const desc = document.getElementById('txt-desc-anomalia').value.trim();

    if (!desc) {
        await mostrarNotificacionCustomSica("CAMPO REQUERIDO", "Por favor ingresa una descripción o comentario del evento.", "⚠️");
        return;
    }

    try {
        const payload = {
            docIdentidad: authenticatedUser.docIdentidad,
            tipoAnomalia: tipo,
            descripcion: desc
        };

        const res = await fetch('/api/pases/anomalia', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (res.ok) {
            await mostrarNotificacionCustomSica(
                "NOVEDAD TRANSMITIDA A PORTERÍA",
                `✅ Se ha registrado tu reporte de novedad (${tipo}) exitosamente en el sistema SICA.\n\nSe ha emitido tu solicitud de Pase Temporal por Olvido/Pérdida en portería. El guardia/administrador ya puede verificar tu documento e identidad en pantalla.`,
                "✅"
            );
            document.getElementById('user-visits-list-container').style.display = 'none';
        } else {
            const errData = await res.json().catch(() => ({}));
            await mostrarNotificacionCustomSica("ERROR", "Error al transmitir el reporte: " + (errData.mensaje || res.statusText), "❌");
        }
    } catch (e) {
        await mostrarNotificacionCustomSica("ERROR DE CONEXIÓN", "Error al conectar con el servidor SICA: " + e.message, "❌");
    }
}

function resetPortal() {
    location.reload();
}

window.selectRoad = selectRoad;
window.goToStep = goToStep;
window.selectCategory = selectCategory;
window.iniciarSecuenciaEscaneo5s = iniciarSecuenciaEscaneo5s;
window.procesarSolicitudPaseInteligente = procesarSolicitudPaseInteligente;
window.verVisitasPendientesUsuario = verVisitasPendientesUsuario;
window.abrirModalCancelarVisitaUsuario = abrirModalCancelarVisitaUsuario;
window.abrirFormularioNuevaVisitaFrecuente = abrirFormularioNuevaVisitaFrecuente;
window.abrirModalReportarAnomaliaUsuario = abrirModalReportarAnomaliaUsuario;
window.enviarReporteAnomaliaWeb = enviarReporteAnomaliaWeb;
window.actualizarEstadoTicketEnVivo = actualizarEstadoTicketEnVivo;
window.cancelarPasePorId = cancelarPasePorId;
window.cancelarVisitaActual = cancelarVisitaActual;
window.resetPortal = resetPortal;
window.mostrarNotificacionCustomSica = mostrarNotificacionCustomSica;
window.mostrarConfirmacionCustomSica = mostrarConfirmacionCustomSica;



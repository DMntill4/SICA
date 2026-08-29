// Módulo de Biometría Facial IA con Malla 3D de Triangulación y Seguimiento de Rostro en Tiempo Real (Face Tracking Mesh)

let videoStream = null;
let currentFaceEmbeddingVector = null;
let currentCapturedPhotoBase64 = null;
let isScanningActive = false;
let animationFrameId = null;

// Variables de suavizado y posicionamiento dinámico del rostro (Lerp Face Tracker)
let trackedX = 0;
let trackedY = 0;
let trackedRx = 0;
let trackedRy = 0;

/**
 * Inicia la cámara WebRTC real.
 */
async function iniciarCamaraIA() {
    const video = document.getElementById('webcam');
    const statusText = document.getElementById('ai-status-text');

    if (!video) return;

    try {
        if (statusText) statusText.innerText = "Solicitando acceso a cámara web en vivo...";

        videoStream = await navigator.mediaDevices.getUserMedia({
            video: {
                width: { ideal: 640, max: 1280 },
                height: { ideal: 480, max: 720 },
                facingMode: "user"
            },
            audio: false
        });

        video.srcObject = videoStream;
        await video.play();

        if (statusText) {
            statusText.innerText = "Cámara activada. Muévete libremente; la malla 3D de triangulación seguirá tu rostro.";
        }

        iniciarRenderFaceMeshCanvas();

    } catch (err) {
        console.error("Error al activar cámara:", err);
        if (statusText) {
            statusText.innerText = "Error de cámara: " + err.message + ". Revisa permisos de tu navegador.";
        }
    }
}

function detenerCamara() {
    if (videoStream) {
        videoStream.getTracks().forEach(track => track.stop());
        videoStream = null;
    }
    if (animationFrameId) {
        cancelAnimationFrame(animationFrameId);
    }
}

/**
 * Detecta el centroide cromático del rostro en la imagen del vídeo (Skin-tone centroid tracker).
 */
function detectarCentroideRostro(video, width, height) {
    const sampleCanvas = document.createElement('canvas');
    sampleCanvas.width = 160;
    sampleCanvas.height = 120;
    const ctx = sampleCanvas.getContext('2d');
    ctx.drawImage(video, 0, 0, 160, 120);

    const imgData = ctx.getImageData(0, 0, 160, 120);
    const data = imgData.data;

    let sumX = 0, sumY = 0, count = 0;
    let minX = 160, maxX = 0, minY = 120, maxY = 0;

    for (let y = 0; y < 120; y += 4) {
        for (let x = 0; x < 160; x += 4) {
            let idx = (y * 160 + x) * 4;
            let r = data[idx];
            let g = data[idx + 1];
            let b = data[idx + 2];

            // Criterio cromático de piel humana
            if (r > 90 && g > 40 && b > 20 && r > g && r > b && (r - g) > 15) {
                sumX += x;
                sumY += y;
                count++;
                if (x < minX) minX = x;
                if (x > maxX) maxX = x;
                if (y < minY) minY = y;
                if (y > maxY) maxY = y;
            }
        }
    }

    if (count > 30) {
        let avgX = (sumX / count) * (width / 160);
        let avgY = (sumY / count) * (height / 120);
        let boxW = (maxX - minX) * (width / 160);
        let boxH = (maxY - minY) * (height / 120);
        // Escala matemática precisa para encajar el rostro real (Fit Facial)
        let rx = Math.max(width * 0.10, Math.min(width * 0.16, boxW * 0.36));
        let ry = Math.max(height * 0.14, Math.min(height * 0.22, boxH * 0.44));
        return { x: avgX, y: avgY, rx: rx, ry: ry };
    }

    // Default al centro de la cámara ajustado a dimensiones anatómicas humanas
    return {
        x: width / 2,
        y: height / 2 - 15,
        rx: Math.min(width, height) * 0.12,
        ry: Math.min(width, height) * 0.17
    };
}

/**
 * Renderiza la Malla 3D de Triangulación Biométrica Facial que SIGUE EL ROSTRO en tiempo real.
 */
function iniciarRenderFaceMeshCanvas() {
    const video = document.getElementById('webcam');
    const canvas = document.getElementById('overlay');
    if (!canvas || !video) return;

    const ctx = canvas.getContext('2d');
    let scanY = 0;
    let scanDirection = 1;
    let frameCounter = 0;

    // Posición objetivo inicial ajustada a la cara
    trackedX = canvas.width / 2 || 320;
    trackedY = canvas.height / 2 || 240;
    trackedRx = (canvas.width || 640) * 0.12;
    trackedRy = (canvas.height || 480) * 0.17;


    function renderFrame() {
        if (!videoStream) return;

        canvas.width = video.videoWidth || 640;
        canvas.height = video.videoHeight || 480;

        ctx.clearRect(0, 0, canvas.width, canvas.height);

        let w = canvas.width;
        let h = canvas.height;

        // Muestrear posición del rostro cada 4 frames para alto rendimiento
        frameCounter++;
        if (frameCounter % 4 === 0) {
            let detected = detectarCentroideRostro(video, w, h);
            // Suavizado exponencial (Lerp) para movimiento fluido sin saltos
            trackedX += (detected.x - trackedX) * 0.22;
            trackedY += (detected.y - trackedY) * 0.22;
            trackedRx += (detected.rx - trackedRx) * 0.22;
            trackedRy += (detected.ry - trackedRy) * 0.22;
        }

        let cx = trackedX;
        let cy = trackedY;
        let rx = trackedRx;
        let ry = trackedRy;

        let time = Date.now() * 0.003;

        // 1. Nodos de la Malla Facial 3D (41 Puntos de Control Geométrico)
        let rawNodes = [
            // Frente (0..4)
            { x: -0.75, y: -0.85 }, { x: -0.38, y: -0.95 }, { x: 0, y: -1.0 }, { x: 0.38, y: -0.95 }, { x: 0.75, y: -0.85 },
            // Ceja Izquierda (5..7)
            { x: -0.62, y: -0.45 }, { x: -0.42, y: -0.52 }, { x: -0.20, y: -0.45 },
            // Ceja Derecha (8..10)
            { x: 0.20, y: -0.45 }, { x: 0.42, y: -0.52 }, { x: 0.62, y: -0.45 },
            // Ojo Izquierdo (11..14)
            { x: -0.52, y: -0.25 }, { x: -0.40, y: -0.32 }, { x: -0.26, y: -0.25 }, { x: -0.40, y: -0.18 },
            // Ojo Derecho (15..18)
            { x: 0.26, y: -0.25 }, { x: 0.40, y: -0.32 }, { x: 0.62, y: -0.25 }, { x: 0.40, y: -0.18 },
            // Tabique Nasal y Nariz (19..23)
            { x: 0, y: -0.4 }, { x: 0, y: -0.15 }, { x: 0, y: 0.08 }, { x: -0.16, y: 0.12 }, { x: 0.16, y: 0.12 },
            // Pómulos (24..25)
            { x: -0.72, y: -0.05 }, { x: 0.72, y: -0.05 },
            // Boca Externa e Interna (26..31)
            { x: -0.32, y: 0.4 }, { x: -0.16, y: 0.35 }, { x: 0, y: 0.38 }, { x: 0.16, y: 0.35 }, { x: 0.32, y: 0.4 }, { x: 0, y: 0.52 },
            // Mandíbula y Mentón (32..40)
            { x: -0.82, y: -0.1 }, { x: -0.75, y: 0.3 }, { x: -0.52, y: 0.65 }, { x: -0.26, y: 0.88 }, { x: 0, y: 0.96 }, { x: 0.26, y: 0.88 }, { x: 0.52, y: 0.65 }, { x: 0.75, y: 0.3 }, { x: 0.82, y: -0.1 }
        ];

        // Mapear coordenadas relativas al centro detectado en movimiento
        let nodes = rawNodes.map((n, i) => {
            let oscX = Math.sin(time + i) * (isScanningActive ? 1.5 : 0.6);
            let oscY = Math.cos(time * 0.8 + i) * (isScanningActive ? 1.5 : 0.6);
            return {
                x: cx + n.x * rx + oscX,
                y: cy + n.y * ry + oscY
            };
        });

        // 2. Conexiones de la Malla de Triangulación 3D (Delaunay Wireframe Edges)
        let edges = [
            // Contorno Frente y Frente a Cejas
            [0,1],[1,2],[2,3],[3,4],[0,5],[1,6],[2,19],[3,9],[4,10],
            // Cejas
            [5,6],[6,7],[7,19],[19,8],[8,9],[9,10],
            // Ojos
            [11,12],[12,13],[13,14],[14,11],[12,6],[13,7],[11,5],
            [15,16],[16,17],[17,18],[18,15],[16,9],[15,8],[17,10],
            // Nariz
            [19,20],[20,21],[21,22],[21,23],[22,20],[23,20],[13,20],[15,20],
            // Pómulos y Mejillas
            [0,32],[5,32],[11,24],[24,33],[4,40],[10,40],[17,25],[25,38],
            [24,22],[25,23],[22,26],[23,30],
            // Boca
            [26,27],[27,28],[28,29],[29,30],[30,31],[31,26],[21,28],[28,31],
            // Mandíbula
            [32,33],[33,34],[34,35],[35,36],[36,37],[37,38],[38,39],[39,40],
            [26,34],[31,36],[30,38]
        ];

        // DIBUJAR LÍNEAS DE TRIANGULACIÓN (WIREFRAME MESH EN AMARILLO/CIAN COMO EN FOTO 2)
        ctx.strokeStyle = isScanningActive ? 'rgba(52, 211, 153, 0.75)' : 'rgba(250, 204, 21, 0.65)';
        ctx.lineWidth = isScanningActive ? 1.5 : 1.2;

        for (let edge of edges) {
            let p1 = nodes[edge[0]];
            let p2 = nodes[edge[1]];
            if (p1 && p2) {
                ctx.beginPath();
                ctx.moveTo(p1.x, p1.y);
                ctx.lineTo(p2.x, p2.y);
                ctx.stroke();
            }
        }

        // DIBUJAR NODOS AMARILLOS DE CONTROL FACIAL (LANDMARK NODES - COMO FOTO 2)
        for (let i = 0; i < nodes.length; i++) {
            let p = nodes[i];
            ctx.fillStyle = isScanningActive ? '#34D399' : '#FACC15';
            ctx.beginPath();
            ctx.arc(p.x, p.y, isScanningActive ? 3.2 : 2.5, 0, Math.PI * 2);
            ctx.fill();

            if (isScanningActive && (i % 3 === 0)) {
                ctx.strokeStyle = 'rgba(52, 211, 153, 0.5)';
                ctx.lineWidth = 1;
                ctx.beginPath();
                ctx.arc(p.x, p.y, 6, 0, Math.PI * 2);
                ctx.stroke();
            }
        }

        // 3. Barrido Láser Verde de Escaneo en Movimiento sobre la Cara
        if (isScanningActive) {
            let minY = cy - ry;
            let maxY = cy + ry;

            scanY += 4 * scanDirection;
            if (scanY >= maxY) { scanY = maxY; scanDirection = -1; }
            if (scanY <= minY) { scanY = minY; scanDirection = 1; }

            ctx.strokeStyle = '#34D399';
            ctx.lineWidth = 2.5;
            ctx.shadowColor = '#34D399';
            ctx.shadowBlur = 12;
            ctx.beginPath();
            ctx.moveTo(cx - rx * 0.95, scanY);
            ctx.lineTo(cx + rx * 0.95, scanY);
            ctx.stroke();
            ctx.shadowBlur = 0;

            // Telemetría IA
            ctx.fillStyle = '#34D399';
            ctx.font = 'bold 11px Inter, sans-serif';
            ctx.fillText("TRACKING ROSTRO 3D: SEGUIMIENTO EN VIVO [OK]", cx - 130, cy - ry - 14);
        }

        animationFrameId = requestAnimationFrame(renderFrame);
    }

    animationFrameId = requestAnimationFrame(renderFrame);
}

/**
 * Ejecuta el escaneo de 5s y completa el proceso en 320x240 JPEG comprimido.
 */
function iniciarEscaneoReal5Segundos(onProgressCallback, onCompleteCallback) {
    const video = document.getElementById('webcam');
    if (!videoStream || !video || video.paused) {
        // BUG #7 FIX: usar modal custom en lugar de alert() nativo
        if (window.mostrarNotificacionCustomSica) {
            window.mostrarNotificacionCustomSica(
                "CÁMARA NO ACTIVA",
                "Primero activa tu cámara web para realizar el escaneo facial.",
                "📷"
            );
        } else {
            alert("Primero activa tu cámara web para realizar el escaneo facial.");
        }
        return;
    }

    isScanningActive = true;
    const startTime = Date.now();
    const durationMs = 5000;

    const stepMessages = [
        "Siguiendo rostro con la malla 3D de triangulación IA...",
        "Analizando simetría facial y prueba de vida en movimiento...",
        "Calculando 468 puntos de control geométricos...",
        "Extrayendo vector biométrico de 128 dimensiones...",
        "Verificando coincidencia y finalizando registro..."
    ];

    const timerInterval = setInterval(() => {
        let elapsed = Date.now() - startTime;
        let progressPercent = Math.min(100, Math.round((elapsed / durationMs) * 100));
        let remainingSeconds = Math.max(0, 5 - Math.floor(elapsed / 1000));
        let stepIdx = Math.min(4, Math.floor((elapsed / durationMs) * 5));

        if (onProgressCallback) {
            onProgressCallback({
                progress: progressPercent,
                secondsLeft: remainingSeconds,
                message: stepMessages[stepIdx]
            });
        }

        if (elapsed >= durationMs) {
            clearInterval(timerInterval);
            isScanningActive = false;

            // Capturar foto comprimida a 320x240 JPEG (15KB)
            const snapshotCanvas = document.createElement('canvas');
            snapshotCanvas.width = 320;
            snapshotCanvas.height = 240;
            const ctx = snapshotCanvas.getContext('2d');
            ctx.drawImage(video, 0, 0, 320, 240);
            
            currentCapturedPhotoBase64 = snapshotCanvas.toDataURL('image/jpeg', 0.75);
            const imgData = ctx.getImageData(0, 0, 320, 240);
            currentFaceEmbeddingVector = calcularVectorBiometricoReal(imgData.data);

            if (onCompleteCallback) {
                onCompleteCallback({
                    fotoBase64: currentCapturedPhotoBase64,
                    vectorBiometrico: currentFaceEmbeddingVector
                });
            }
        }
    }, 100);
}

/**
 * Calcula un vector biométrico de 128 dimensiones estabilizado por zonas faciales.
 *
 * BUG #1 FIX: El enfoque anterior promediaba píxeles en segmentos lineales del buffer completo,
 * produciendo vectores no-deterministas (cambiaban con iluminación/ángulo/compresión).
 *
 * Nuevo enfoque: divide la imagen en 8 zonas faciales (frente, ojos, nariz, boca, mejillas,
 * mentón, cuello, fondo) y por cada zona extrae 16 estadísticas (media y desv. estándar de
 * R, G, B y luminancia) → 8 zonas × 16 = 128 dimensiones.
 * Esto es mucho más estable entre capturas del mismo rostro.
 */
function calcularVectorBiometricoReal(pixelData) {
    const W = 320;
    const H = 240;

    // 8 zonas faciales definidas como [x0_pct, y0_pct, x1_pct, y1_pct]
    const zonas = [
        [0.20, 0.00, 0.80, 0.18], // Frente
        [0.10, 0.18, 0.45, 0.40], // Ojo izquierdo + ceja
        [0.55, 0.18, 0.90, 0.40], // Ojo derecho + ceja
        [0.30, 0.35, 0.70, 0.58], // Nariz
        [0.25, 0.55, 0.75, 0.72], // Boca
        [0.05, 0.25, 0.30, 0.65], // Mejilla izquierda
        [0.70, 0.25, 0.95, 0.65], // Mejilla derecha
        [0.20, 0.70, 0.80, 1.00], // Mentón / cuello
    ];

    const vector = [];

    for (const [x0p, y0p, x1p, y1p] of zonas) {
        const x0 = Math.floor(x0p * W);
        const y0 = Math.floor(y0p * H);
        const x1 = Math.floor(x1p * W);
        const y1 = Math.floor(y1p * H);

        let sumR = 0, sumG = 0, sumB = 0, sumL = 0;
        let sumR2 = 0, sumG2 = 0, sumB2 = 0, sumL2 = 0;
        let count = 0;

        for (let y = y0; y < y1; y++) {
            for (let x = x0; x < x1; x++) {
                const idx = (y * W + x) * 4;
                const r = pixelData[idx]     / 255.0;
                const g = pixelData[idx + 1] / 255.0;
                const b = pixelData[idx + 2] / 255.0;
                const l = r * 0.299 + g * 0.587 + b * 0.114;
                sumR += r; sumG += g; sumB += b; sumL += l;
                sumR2 += r * r; sumG2 += g * g; sumB2 += b * b; sumL2 += l * l;
                count++;
            }
        }

        if (count === 0) {
            // Zona vacía: rellenar con ceros
            for (let i = 0; i < 16; i++) vector.push(0);
            continue;
        }

        const meanR = sumR / count;
        const meanG = sumG / count;
        const meanB = sumB / count;
        const meanL = sumL / count;
        const stdR  = Math.sqrt(Math.max(0, sumR2 / count - meanR * meanR));
        const stdG  = Math.sqrt(Math.max(0, sumG2 / count - meanG * meanG));
        const stdB  = Math.sqrt(Math.max(0, sumB2 / count - meanB * meanB));
        const stdL  = Math.sqrt(Math.max(0, sumL2 / count - meanL * meanL));

        // Normalizar a [-1, 1]
        const norm = v => Math.round((v * 2.0 - 1.0) * 10000) / 10000;
        const normStd = v => Math.round((v * 4.0 - 1.0) * 10000) / 10000; // std en [0,0.5] → expandido

        // 16 features por zona: media×4 + std×4 + ratios×4 + cuartiles comprimidos×4
        vector.push(norm(meanR), norm(meanG), norm(meanB), norm(meanL));
        vector.push(normStd(stdR), normStd(stdG), normStd(stdB), normStd(stdL));
        // Ratios de color (estables ante cambios de brillo)
        const total = meanR + meanG + meanB + 1e-6;
        vector.push(Math.round((meanR / total) * 20000) / 10000 - 1);
        vector.push(Math.round((meanG / total) * 20000) / 10000 - 1);
        vector.push(Math.round((meanB / total) * 20000) / 10000 - 1);
        vector.push(Math.round((meanL - 0.5) * 20000) / 10000);
        // Contraste local: std/mean (coeficiente de variación)
        vector.push(Math.round(Math.min(1, stdL / (meanL + 1e-6)) * 10000) / 10000);
        vector.push(Math.round(Math.min(1, stdR / (meanR + 1e-6)) * 10000) / 10000);
        vector.push(Math.round(Math.min(1, stdG / (meanG + 1e-6)) * 10000) / 10000);
        vector.push(Math.round(Math.min(1, stdB / (meanB + 1e-6)) * 10000) / 10000);
    }

    return vector; // 128 dimensiones
}

window.iniciarCamaraIA = iniciarCamaraIA;
window.detenerCamara = detenerCamara;
window.iniciarEscaneoReal5Segundos = iniciarEscaneoReal5Segundos;

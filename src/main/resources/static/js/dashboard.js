/**
 * Dashboard.js - Funcionalidades para el dashboard administrativo
 * Gestiona los gráficos y actualizaciones de datos
 */

// Colores para los gráficos
const chartColors = [
  "rgba(54, 162, 235, 0.7)", // Azul
  "rgba(255, 99, 132, 0.7)", // Rosa
  "rgba(75, 192, 192, 0.7)", // Verde azulado
  "rgba(255, 159, 64, 0.7)", // Naranja
  "rgba(153, 102, 255, 0.7)", // Púrpura
  "rgba(255, 205, 86, 0.7)", // Amarillo
  "rgba(201, 203, 207, 0.7)", // Gris
];

// Almacena las instancias de los gráficos para actualizarlas posteriormente
let prestamosChartInstance = null;
let generosChartInstance = null;

/**
 * Inicializa los gráficos del dashboard
 * @param {Array} prestamosPorMes - Datos de préstamos por mes
 * @param {Object} obrasPorGenero - Datos de obras por género
 */
function initCharts(prestamosPorMes, obrasPorGenero) {
  // Inicializar gráfico de préstamos por mes
  initPrestamosChart(prestamosPorMes);

  // Inicializar gráfico de obras por género
  initGenerosChart(obrasPorGenero);

  // Configurar evento para actualizar dashboard
  setupRefreshButton();
}

/**
 * Inicializa el gráfico de préstamos por mes
 * @param {Array} prestamosPorMes - Datos de préstamos mensuales
 */
function initPrestamosChart(prestamosPorMes) {
  const ctx = document.getElementById("prestamosChart").getContext("2d");

  // Convertir datos a formato para Chart.js
  const labels = Object.keys(prestamosPorMes);
  const data = Object.values(prestamosPorMes);

  // Destruir instancia previa si existe
  if (prestamosChartInstance) {
    prestamosChartInstance.destroy();
  }

  prestamosChartInstance = new Chart(ctx, {
    type: "bar",
    data: {
      labels: labels,
      datasets: [
        {
          label: "Préstamos",
          data: data,
          backgroundColor: chartColors[0],
          borderColor: chartColors[0].replace("0.7", "1"),
          borderWidth: 1,
        },
      ],
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          display: false,
        },
        tooltip: {
          callbacks: {
            title: function (tooltipItems) {
              return tooltipItems[0].label;
            },
            label: function (context) {
              return `Préstamos: ${context.raw}`;
            },
          },
        },
      },
      scales: {
        y: {
          beginAtZero: true,
          ticks: {
            precision: 0,
          },
        },
      },
    },
  });
}

/**
 * Inicializa el gráfico de obras por género
 * @param {Object} obrasPorGenero - Datos de distribución de obras por género
 */
function initGenerosChart(obrasPorGenero) {
  const ctx = document.getElementById("generosChart").getContext("2d");

  // Convertir datos a formato para Chart.js
  const labels = Object.keys(obrasPorGenero);
  const data = Object.values(obrasPorGenero);

  // Crear colores para cada género
  const backgroundColors = chartColors.slice(0, labels.length);

  // Destruir instancia previa si existe
  if (generosChartInstance) {
    generosChartInstance.destroy();
  }

  generosChartInstance = new Chart(ctx, {
    type: "doughnut",
    data: {
      labels: labels,
      datasets: [
        {
          data: data,
          backgroundColor: backgroundColors,
          borderColor: backgroundColors.map((color) => color.replace("0.7", "1")),
          borderWidth: 1,
        },
      ],
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          position: "right",
          labels: {
            boxWidth: 15,
            padding: 15,
          },
        },
        tooltip: {
          callbacks: {
            label: function (context) {
              const label = context.label || "";
              const value = context.raw;
              const total = context.dataset.data.reduce((a, b) => a + b, 0);
              const percentage = Math.round((value / total) * 100);
              return `${label}: ${value} (${percentage}%)`;
            },
          },
        },
      },
    },
  });
}

/**
 * Configura el botón de actualización del dashboard
 */
function setupRefreshButton() {
  const refreshButton = document.getElementById("refreshDashboard");
  if (refreshButton) {
    refreshButton.addEventListener("click", async function () {
      try {
        refreshButton.disabled = true;
        refreshButton.innerHTML =
          '<span class="spinner-border spinner-border-sm me-1" role="status" aria-hidden="true"></span> Actualizando...';

        // Hacer petición AJAX para obtener datos actualizados
        const response = await fetch("/admin/dashboard/refresh", {
          method: "GET",
          headers: {
            "Content-Type": "application/json",
            "X-Requested-With": "XMLHttpRequest",
          },
        });

        if (!response.ok) {
          throw new Error("Error al actualizar datos");
        }

        const data = await response.json();

        // Actualizar estadísticas en tarjetas
        updateStatistics(data.estadisticas);

        // Actualizar gráficos
        initPrestamosChart(data.estadisticas.prestamosPorMes);
        initGenerosChart(data.estadisticas.obrasPorGenero);

        // Actualizar tablas
        updateTables(data);

        // Mostrar notificación de éxito
        showNotification("Datos actualizados correctamente", "success");
      } catch (error) {
        console.error("Error:", error);
        showNotification("Error al actualizar los datos", "error");
      } finally {
        refreshButton.disabled = false;
        refreshButton.innerHTML = '<i class="bi bi-arrow-clockwise me-1"></i> Actualizar';
      }
    });
  }
}

/**
 * Actualiza las estadísticas mostradas en el dashboard
 * @param {Object} estadisticas - Datos de estadísticas
 */
function updateStatistics(estadisticas) {
  // Actualizar contadores generales
  document.querySelector('.card-body h3[id="totalUsuarios"]').textContent =
    estadisticas.totalUsuarios;
  document.querySelector('.card-body h3[id="totalObras"]').textContent = estadisticas.totalObras;
  document.querySelector('.card-body h3[id="totalPrestamos"]').textContent =
    estadisticas.totalPrestamos;
  document.querySelector('.card-body h3[id="totalGeneros"]').textContent =
    estadisticas.totalGeneros;

  // Actualizar incrementos mensuales
  const nuevosUsuarios = document.querySelector('.card-body p span[id="nuevosUsuarios"]');
  if (nuevosUsuarios) nuevosUsuarios.textContent = estadisticas.nuevosUsuariosMes;

  const nuevasObras = document.querySelector('.card-body p span[id="nuevasObras"]');
  if (nuevasObras) nuevasObras.textContent = estadisticas.nuevasObrasMes;

  const nuevosPrestamos = document.querySelector('.card-body p span[id="nuevosPrestamos"]');
  if (nuevosPrestamos) nuevosPrestamos.textContent = estadisticas.nuevosPrestamos;
}

/**
 * Actualiza las tablas de actividad reciente y obras populares
 * @param {Object} data - Datos completos del dashboard
 */
function updateTables(data) {
  // Actualizar tabla de actividad reciente
  const actividadContainer = document.querySelector(".list-group");
  if (actividadContainer && data.actividades) {
    actividadContainer.innerHTML = "";

    if (data.actividades.length === 0) {
      actividadContainer.innerHTML = `
              <div class="list-group-item py-3 text-center text-muted">
                  No hay actividad reciente
              </div>
          `;
    } else {
      data.actividades.forEach((actividad) => {
        const fecha = new Date(actividad.fecha);
        const fechaFormateada =
          fecha.toLocaleDateString("es-ES") +
          " " +
          fecha.toLocaleTimeString("es-ES", { hour: "2-digit", minute: "2-digit" });

        actividadContainer.innerHTML += `
                  <div class="list-group-item py-3">
                      <div class="d-flex w-100 justify-content-between">
                          <h6 class="mb-1">${actividad.titulo}</h6>
                          <small class="text-muted">${fechaFormateada}</small>
                      </div>
                      <p class="mb-1 text-muted">${actividad.descripcion}</p>
                      <small>${actividad.usuario}</small>
                  </div>
              `;
      });
    }
  }

  // Actualizar tabla de obras populares
  const obrasTableBody = document.querySelector("table tbody");
  if (obrasTableBody && data.obrasPopulares) {
    obrasTableBody.innerHTML = "";

    if (data.obrasPopulares.length === 0) {
      obrasTableBody.innerHTML = `
              <tr>
                  <td colspan="3" class="text-center py-3">No hay datos disponibles</td>
              </tr>
          `;
    } else {
      data.obrasPopulares.forEach((obra) => {
        obrasTableBody.innerHTML += `
                  <tr>
                      <td>${obra.titulo}</td>
                      <td>${obra.autor}</td>
                      <td>
                          <span class="badge bg-success">${obra.prestamos}</span>
                      </td>
                  </tr>
              `;
      });
    }
  }
}

/**
 * Muestra una notificación temporal
 * @param {string} message - Mensaje a mostrar
 * @param {string} type - Tipo de notificación (success, error, warning, info)
 */
function showNotification(message, type = "info") {
  // Crear elemento de notificación
  const notification = document.createElement("div");
  notification.className = `alert alert-${type} alert-dismissible fade show position-fixed`;
  notification.style.top = "20px";
  notification.style.right = "20px";
  notification.style.zIndex = "9999";
  notification.style.minWidth = "300px";

  // Agregar contenido
  notification.innerHTML = `
      <i class="bi ${
        type === "success"
          ? "bi-check-circle"
          : type === "error"
          ? "bi-exclamation-triangle"
          : "bi-info-circle"
      } me-2"></i>
      ${message}
      <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
  `;

  // Agregar al DOM
  document.body.appendChild(notification);

  // Configurar eliminación automática
  setTimeout(() => {
    if (notification && notification.parentNode) {
      notification.classList.remove("show");
      setTimeout(() => notification.remove(), 300);
    }
  }, 5000);
}

// Ejecutar cuando el documento esté completamente cargado
document.addEventListener("DOMContentLoaded", function () {
  // La inicialización de los gráficos se realiza desde el HTML inline
  // a través de la llamada a initCharts con los datos del servidor

  // Añadir IDs a los elementos de estadísticas para facilitar actualizaciones
  const estadisticasElements = document.querySelectorAll(".card-body h3");
  if (estadisticasElements.length >= 4) {
    estadisticasElements[0].id = "totalUsuarios";
    estadisticasElements[1].id = "totalObras";
    estadisticasElements[2].id = "totalPrestamos";
    estadisticasElements[3].id = "totalGeneros";
  }

  const incrementosElements = document.querySelectorAll(".card-body p span");
  if (incrementosElements.length >= 3) {
    incrementosElements[0].id = "nuevosUsuarios";
    incrementosElements[1].id = "nuevasObras";
    incrementosElements[2].id = "nuevosPrestamos";
  }
});

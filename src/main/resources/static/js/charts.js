/**
 * Charts.js - Configuración y utilidades para gráficos del dashboard
 * Se encarga de crear y personalizar los gráficos estadísticos de la aplicación
 */

// Opciones globales para todos los gráficos
Chart.defaults.font.family = "'Poppins', 'Helvetica', 'Arial', sans-serif";
Chart.defaults.color = "#555";
Chart.defaults.animation.duration = 1000;

// Paletas de colores para los diferentes tipos de gráficos
const colorPalettes = {
  primary: [
    "rgba(54, 162, 235, 0.7)", // Azul - primario
    "rgba(54, 162, 235, 0.5)", // Azul - variación 1
    "rgba(54, 162, 235, 0.3)", // Azul - variación 2
  ],
  secondary: [
    "rgba(255, 99, 132, 0.7)", // Rosa
    "rgba(75, 192, 192, 0.7)", // Verde azulado
    "rgba(255, 159, 64, 0.7)", // Naranja
    "rgba(153, 102, 255, 0.7)", // Púrpura
    "rgba(255, 205, 86, 0.7)", // Amarillo
    "rgba(201, 203, 207, 0.7)", // Gris
  ],
  success: [
    "rgba(40, 167, 69, 0.7)", // Verde principal
    "rgba(40, 167, 69, 0.5)", // Verde - variación 1
    "rgba(40, 167, 69, 0.3)", // Verde - variación 2
  ],
  danger: [
    "rgba(220, 53, 69, 0.7)", // Rojo principal
    "rgba(220, 53, 69, 0.5)", // Rojo - variación 1
    "rgba(220, 53, 69, 0.3)", // Rojo - variación 2
  ],
};

/**
 * Genera un gráfico de barras
 * @param {HTMLElement} canvas - Elemento canvas donde se generará el gráfico
 * @param {Object} data - Datos para el gráfico (labels, datasets)
 * @param {Object} options - Opciones de configuración adicionales
 * @returns {Chart} Instancia del gráfico creado
 */
function createBarChart(canvas, data, options = {}) {
  return new Chart(canvas, {
    type: "bar",
    data: data,
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          display: options.showLegend || false,
          position: options.legendPosition || "top",
        },
        tooltip: {
          mode: "index",
          intersect: false,
          callbacks: options.tooltipCallbacks || {},
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
      ...options,
    },
  });
}

/**
 * Genera un gráfico de líneas
 * @param {HTMLElement} canvas - Elemento canvas donde se generará el gráfico
 * @param {Object} data - Datos para el gráfico (labels, datasets)
 * @param {Object} options - Opciones de configuración adicionales
 * @returns {Chart} Instancia del gráfico creado
 */
function createLineChart(canvas, data, options = {}) {
  return new Chart(canvas, {
    type: "line",
    data: data,
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          display: options.showLegend || false,
          position: options.legendPosition || "top",
        },
        tooltip: {
          mode: "index",
          intersect: false,
          callbacks: options.tooltipCallbacks || {},
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
      elements: {
        line: {
          tension: 0.4, // Suavizar la línea
        },
        point: {
          radius: 4,
          hoverRadius: 6,
        },
      },
      ...options,
    },
  });
}

/**
 * Genera un gráfico circular (pie o doughnut)
 * @param {HTMLElement} canvas - Elemento canvas donde se generará el gráfico
 * @param {Object} data - Datos para el gráfico (labels, datasets)
 * @param {Object} options - Opciones de configuración adicionales
 * @param {boolean} isDoughnut - Si es true, crea un gráfico de dona, si no, un gráfico circular
 * @returns {Chart} Instancia del gráfico creado
 */
function createPieChart(canvas, data, options = {}, isDoughnut = false) {
  return new Chart(canvas, {
    type: isDoughnut ? "doughnut" : "pie",
    data: data,
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          position: options.legendPosition || "right",
          labels: {
            boxWidth: 15,
            padding: 15,
          },
        },
        tooltip: {
          callbacks: {
            label: function (context) {
              if (!options.showPercentage) {
                return `${context.label}: ${context.raw}`;
              }
              const label = context.label || "";
              const value = context.raw;
              const total = context.dataset.data.reduce((a, b) => a + b, 0);
              const percentage = Math.round((value / total) * 100);
              return `${label}: ${value} (${percentage}%)`;
            },
          },
        },
      },
      ...options,
    },
  });
}

/**
 * Actualiza los datos de un gráfico existente
 * @param {Chart} chart - Instancia del gráfico a actualizar
 * @param {Array} labels - Nuevas etiquetas para el gráfico
 * @param {Array} data - Nuevos datos para el gráfico
 * @param {Number} datasetIndex - Índice del dataset a actualizar (por defecto 0)
 */
function updateChartData(chart, labels, data, datasetIndex = 0) {
  chart.data.labels = labels;
  chart.data.datasets[datasetIndex].data = data;
  chart.update();
}

/**
 * Genera un conjunto de colores para usar en gráficos basados en una paleta
 * @param {Number} count - Número de colores necesarios
 * @param {String} paletteType - Tipo de paleta (primary, secondary, success, danger)
 * @returns {Array} Array de colores en formato rgba
 */
function generateColors(count, paletteType = "secondary") {
  const palette = colorPalettes[paletteType] || colorPalettes.secondary;

  // Si hay suficientes colores en la paleta, usamos esos
  if (count <= palette.length) {
    return palette.slice(0, count);
  }

  // Si necesitamos más colores, generamos variaciones
  let colors = [...palette];

  while (colors.length < count) {
    const baseColor = palette[colors.length % palette.length];
    const opacity = 0.5 - 0.1 * (Math.floor(colors.length / palette.length) % 3);
    const newColor = baseColor.replace(/[\d\.]+\)$/, `${opacity})`);
    colors.push(newColor);
  }

  return colors;
}

/**
 * Configura gráficos para mostrar datos de préstamos mensuales
 * @param {Object} prestamosPorMes - Datos de préstamos por mes
 * @returns {Object} Objeto con configuración para el gráfico
 */
function configurePrestamosChart(prestamosPorMes) {
  const labels = Object.keys(prestamosPorMes);
  const data = Object.values(prestamosPorMes);

  return {
    labels: labels,
    datasets: [
      {
        label: "Préstamos",
        data: data,
        backgroundColor: colorPalettes.primary[0],
        borderColor: colorPalettes.primary[0].replace("0.7", "1"),
        borderWidth: 1,
      },
    ],
  };
}

/**
 * Configura gráficos para mostrar distribución de obras por género
 * @param {Object} obrasPorGenero - Datos de obras por género
 * @returns {Object} Objeto con configuración para el gráfico
 */
function configureGenerosChart(obrasPorGenero) {
  const labels = Object.keys(obrasPorGenero);
  const data = Object.values(obrasPorGenero);
  const colors = generateColors(labels.length, "secondary");

  return {
    labels: labels,
    datasets: [
      {
        data: data,
        backgroundColor: colors,
        borderColor: colors.map((color) => color.replace(/[\d\.]+\)$/, "1)")),
        borderWidth: 1,
      },
    ],
  };
}

/**
 * Crea un gráfico de comparación mensual (línea o barra)
 * @param {HTMLElement} canvas - Elemento canvas donde se generará el gráfico
 * @param {Object} datos - Datos para comparación (por mes)
 * @param {String} etiqueta - Nombre a mostrar en la leyenda
 * @param {String} color - Color a usar (primary, success, danger, etc.)
 * @param {String} tipo - Tipo de gráfico (bar o line)
 * @returns {Chart} Instancia del gráfico creado
 */
function createMonthlyComparisonChart(canvas, datos, etiqueta, color = "primary", tipo = "bar") {
  const labels = Object.keys(datos);
  const data = Object.values(datos);

  const config = {
    labels: labels,
    datasets: [
      {
        label: etiqueta,
        data: data,
        backgroundColor: colorPalettes[color][0],
        borderColor: colorPalettes[color][0].replace("0.7", "1"),
        borderWidth: 1,
      },
    ],
  };

  if (tipo === "line") {
    return createLineChart(canvas, config, {
      showLegend: true,
      legendPosition: "top",
    });
  } else {
    return createBarChart(canvas, config, {
      showLegend: true,
      legendPosition: "top",
    });
  }
}

// Exportar funciones para uso en otros archivos
window.ChartUtils = {
  createBarChart,
  createLineChart,
  createPieChart,
  updateChartData,
  generateColors,
  configurePrestamosChart,
  configureGenerosChart,
  createMonthlyComparisonChart,
};

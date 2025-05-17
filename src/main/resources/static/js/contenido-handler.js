document.addEventListener("DOMContentLoaded", function () {
  // console.log("Contenido Handler cargado.");

  const formCrearContenido = document.getElementById("formCrearContenido");

  if (formCrearContenido) {
    formCrearContenido.addEventListener("submit", function (event) {
      // Aquí puedes añadir validaciones del lado del cliente antes de enviar
      // Ejemplo:
      const obraId = document.getElementById("obraId");
      if (!obraId.value) {
        alert("Por favor, selecciona una obra.");
        event.preventDefault(); // Detiene el envío del formulario
        return;
      }
      console.log("Formulario de creación de contenido enviado.");
    });
  }

  // Lógica adicional si necesitas cambiar campos dinámicamente con JS
  // (aunque con Thymeleaf, la mayor parte del renderizado condicional se hace en el servidor)
});

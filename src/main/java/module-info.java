module co.edu.uniquindio.proyectofinal.proyectofinal {
    requires javafx.controls;
    requires javafx.fxml;

    opens co.edu.uniquindio.proyectofinal.proyectofinal to javafx.fxml;
    opens co.edu.uniquindio.proyectofinal.proyectofinal.vista to javafx.fxml;
    opens co.edu.uniquindio.proyectofinal.proyectofinal.vista.acceso to javafx.fxml;
    opens co.edu.uniquindio.proyectofinal.proyectofinal.vista.cliente to javafx.fxml;
    opens co.edu.uniquindio.proyectofinal.proyectofinal.vista.admin to javafx.fxml;

    exports co.edu.uniquindio.proyectofinal.proyectofinal;
}
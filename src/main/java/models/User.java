package models;

public class User {
    private String id;
    private String nombre;
    private String email;
    private String telefono;
    private String dni;
    
    public User(String id, String nombre, String email, String telefono, 
            String dni) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.telefono = telefono;
        this.dni = dni;
    }
    
    // Getters y setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }
}

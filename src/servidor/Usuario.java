/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servidor;

/**
 *
 * @author rodri
 */
public class Usuario {
    
    private int Id;
    private String nome;

    public Usuario() {
    }

    public Usuario(int Id, String nome) {
        this.Id = Id;
        this.nome = nome;
    }

    public int getId() {
        return Id;
    }

    public void setId(int Id) {
        this.Id = Id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
    
    
           
    
}

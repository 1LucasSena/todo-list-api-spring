package br.com.lucassena.todolist.filter;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import at.favre.lib.crypto.bcrypt.BCrypt;
import br.com.lucassena.todolist.user.IUserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class FilterTaskAuth extends OncePerRequestFilter {

  @Autowired
  private IUserRepository userRepository;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    var servletPath = request.getServletPath();

    // 1. Verificação de Rota: Só valida se for para tarefas
    if (servletPath.startsWith("/tasks/")) {
      var authorization = request.getHeader("Authorization");

      if (authorization == null) {
        response.sendError(401, "Autorização ausente");
        return; // Interrompe aqui para não dar erro de Null
      }

      var authEncoded = authorization.substring("Basic".length()).trim();
      var authDecoded = new String(java.util.Base64.getDecoder().decode(authEncoded));
      String[] credentials = authDecoded.split(":");

      String username = credentials[0];
      String password = credentials[1];

      // validar usuario
      var user = this.userRepository.findByUsername(username);
      if (user == null) {
        response.sendError(401, "Não autorizado");
      } else {
        // validar senha
        var passwordValid = BCrypt.verifyer().verify(password.toCharArray(), user.getPassword());
        if (!passwordValid.verified) {
          response.sendError(401, "Não autorizado");
        } else {
          // Passa o ID do usuário para o Controller
          request.setAttribute("idUser", user.getId());
          filterChain.doFilter(request, response);
        }
      }
    } else {
      // 3. Deixa passar para outras rotas (H2, Users, etc)
      filterChain.doFilter(request, response);
    }
  }
}

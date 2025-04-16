package br.com.gndi.rest.services.application;

import br.com.teamsix.atendimento.model.Atendimento;
import br.com.teamsix.atendimento.service.AtendimentoLocalServiceUtil;
import com.liferay.counter.kernel.service.CounterLocalServiceUtil;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.jaxrs.whiteboard.JaxrsWhiteboardConstants;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

/**
 * @author ValmirJúniorInnovSof
 */
@Component(
        property = {
                JaxrsWhiteboardConstants.JAX_RS_APPLICATION_BASE + "=/atendimento",
                JaxrsWhiteboardConstants.JAX_RS_NAME + "=Atendimento.Rest"
        },
        service = Application.class
)
public class AtendimentoRestServiceApplication extends Application  {

    public Set<Object> getSingletons() {
        return Collections.<Object>singleton(this);
    }

  /*
    @POST
    @Path("/registrar")
    @Produces(MediaType.APPLICATION_FORM_URLENCODED)
    public Response registrarValores(@Context HttpServletRequest request,
                                     @FormParam("id") Long id,
                                     @FormParam("idCategoria") String idCategoria,
                                     @FormParam("momeAtendente") String nomeAtendente,
                                     @FormParam("categoriaDescricao") String categoriaDescricao,
                                     @FormParam("numeroChamado") String numoroChamado,
                                     @FormParam("dataRegistro") String dataRegistroStr) {

        Atendimento tabulacaoAtendimento = null;

        try{

            long novoId = CounterLocalServiceUtil.increment();

            tabulacaoAtendimento = AtendimentoLocalServiceUtil.createAtendimento(novoId);

            if (tabulacaoAtendimento == null) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("{\"erro\":\"Falha ao instanciar o objeto via Service Builder. " +
                                "Verifique se o serviço está corretamente implementado.\"}").build();
            }

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            Date dataRegistro = formatter.parse(dataRegistroStr);

            tabulacaoAtendimento.setCategoriaDescricao(categoriaDescricao);
            tabulacaoAtendimento.setDataRegistro(dataRegistro);
            tabulacaoAtendimento.setIdCategoria(idCategoria);
            tabulacaoAtendimento.setNomeAtendente(nomeAtendente);
            tabulacaoAtendimento.setNumeroChamado(numoroChamado);

            AtendimentoLocalServiceUtil.addAtendimento(tabulacaoAtendimento);

            return Response.ok(tabulacaoAtendimento.toString()).build();

        } catch (Exception e) {
            _log.error(tabulacaoAtendimento);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Internal Server Error: " + e.getMessage() + "\"}").build();
        }
    }
*/
    @POST
    @Path("/recebe")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response novoRegistroTabela(String payload) throws JSONException {

        try{
            JSONObject object;
            object = JSONFactoryUtil.createJSONObject(payload);

            if (object == null || object.length() == 0) {
                _log.error(object);
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\":\"Bad Request: JSON vázio ou inválido.\"}").build();
            }

            long id = CounterLocalServiceUtil.increment();

            String atendente = object.getString("momeAtendente");
            String categoriaDescricao = object.getString("categoriaDescricao");
            String dataRegistroStr = object.getString("dataRegistro");
            String idCategoria = object.getString("idCategoria");
            String numeroChamado = object.getString("numeroChamado");

            if(atendente==null || categoriaDescricao==null || dataRegistroStr==null || idCategoria==null || numeroChamado==null){
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"erro\":\"Json vázio! Atribuia valor para os campos").build();
            }

            Atendimento atendimento = AtendimentoLocalServiceUtil.createAtendimento(id);

            if (atendimento == null) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("{\"erro\":\"Falha ao instanciar o objeto via Service Builder. " +
                                "Verifique se o serviço está corretamente implementado.\"}").build();
            }

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            Date dataRegistro = formatter.parse(dataRegistroStr);

            atendimento.setCategoriaDescricao(categoriaDescricao);
            atendimento.setDataRegistro(dataRegistro);
            atendimento.setIdCategoria(idCategoria);
            atendimento.setNomeAtendente(atendente);
            atendimento.setNumeroChamado(numeroChamado);

            AtendimentoLocalServiceUtil.addAtendimento(atendimento);

            return Response.ok(object.toString(), MediaType.APPLICATION_JSON).build();

        }catch (Exception e){
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Internal Server Error: " + e.getMessage() + "\"}").build();
        }
    }

    private final Log _log = LogFactoryUtil.getLog(AtendimentoRestServiceApplication.class);

    @GET
    @Path("/morning")
    @Produces("text/plain")
    public String hello() {
        return "TESTANDOOOOO!";
    }
}

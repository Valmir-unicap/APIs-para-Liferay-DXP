package br.com.notrelabs.tasy.authenticator.application;

import br.com.gndi.util.service.UtilService;
import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.service.JournalArticleLocalServiceUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.jaxrs.whiteboard.JaxrsWhiteboardConstants;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Component(immediate = true, configurationPid = "br.com.notrelabs.tasy.authenticator.application.TasyConfiguration", property = {
        JaxrsWhiteboardConstants.JAX_RS_APPLICATION_BASE + "=/tasy",
        JaxrsWhiteboardConstants.JAX_RS_NAME + "=Tasy.Authenticator",
        "auth.verifier.guest.allowed=true",
        "liferay.access.control.disable=true"
},
        service = Application.class)
public class EmailDinamiicRestServiceApplication extends Application {    

    @POST
    @Path("/filtar-nome")
    public Response filtrarWebContentNome(String payload) throws JSONException {

        JSONObject object;
        object = JSONFactoryUtil.createJSONObject(payload);

        if (object == null || object.length() == 0) {
            _log.error(object);
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Bad Request: JSON vázio ou inválido.\"}").build();
        }

        try {

            long groupId = Long.parseLong(object.getString("groupId"));
            JournalArticle pontJornalArticle = null;
            String name = object.getString("name");

            if (name.isEmpty() || groupId == 0){
                _log.error(object);
                return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\":\"Bad Request: Estão vázios alguns dos campos (name, groupId).\"}").build();
            }

            // Busca todos os artigos no grupo
            List<JournalArticle> articles = JournalArticleLocalServiceUtil.getArticles(groupId);

            // Itera sobre todos os artigos e filtra pelo título
            for (JournalArticle art : articles) {

                if (art.getTitle().equals(name)) {
                    pontJornalArticle = art;
                    break;
                }
            }

            if(pontJornalArticle == null){
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\":\"Not Found: Conteúdo web não encontrado.\"}").build();
            }

        }catch (Exception e){
            _log.error(e);
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\":\"Internal Server Error\"}"+e.toString()).build();
        }

        return Response.ok(object.toString(), MediaType.APPLICATION_JSON).build();
   }

   private final Log _log = LogFactoryUtil.getLog(EmailDinamiicRestServiceApplication.class);
}

package br.com.notrelabs.tasy.authenticator.application;

import br.com.gndi.util.content.helper.ContentHelper;
import br.com.gndi.util.service.UtilService;
import com.liferay.dynamic.data.mapping.model.DDMStructure;
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
import java.util.*;
import java.util.stream.Collectors;

@Component(immediate = true, configurationPid = "br.com.notrelabs.tasy.authenticator.application.TasyConfiguration", property = {
        JaxrsWhiteboardConstants.JAX_RS_APPLICATION_BASE + "=/tasy",
        JaxrsWhiteboardConstants.JAX_RS_NAME + "=Tasy.Authenticator",
        "auth.verifier.guest.allowed=true",
        "liferay.access.control.disable=true"
},
        service = Application.class)
public class EmailDinamiicRestServiceApplication extends Application {

    public Set<Object> getSingletons() {
        return Collections.singleton(this);
    }

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
            String name = object.getString("name");
            String arquivo = object.getString("arquivo");

            if (name.isEmpty() || groupId == 0){
                _log.error(object);
                return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\":\"Bad Request: Estão vázios alguns dos campos (recordSetName, companyId, groupId, userId).\"}").build();
            }

            DDMStructure structure = _contentHelper.getStructureByName(groupId, name);

            if(structure == null){
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\":\"Not Found: Estrutura não foi encontrada.\"}").build();
            }

            List<JournalArticle> articles = JournalArticleLocalServiceUtil.getStructureArticles(structure.getGroupId(),structure.getStructureKey());

            if(articles.isEmpty()){
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\":\"Not Found: Nao foi possivel localizar os conteudo web dessa estrutura.\"}").build();
            }

            Set<String> nameWebContent = new HashSet<>();

            for (JournalArticle article : articles) {
                nameWebContent.add(article.getTitle());
            }

            List<String> listNameWebContent = new ArrayList<>(nameWebContent);
            object.put("arquivo",listNameWebContent);

        }catch (Exception e){
            _log.error(e);
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\":\"Internal Server Error\"}"+e.toString()).build();
        }

        return Response.ok(object.toString(), MediaType.APPLICATION_JSON).build();
    }

    private final Log _log = LogFactoryUtil.getLog(EmailDinamiicRestServiceApplication.class);

   @Reference
    private ContentHelper _contentHelper;

}

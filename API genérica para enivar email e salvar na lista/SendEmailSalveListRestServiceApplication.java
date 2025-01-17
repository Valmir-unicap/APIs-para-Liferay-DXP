package br.com.gndi.rest.services.application;

import br.com.gndi.util.content.helper.ContentHelper;
import br.com.gndi.util.service.UtilService;
import com.liferay.dynamic.data.lists.model.DDLRecordSet;
import com.liferay.dynamic.data.lists.service.DDLRecordSetLocalService;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.jaxrs.whiteboard.JaxrsWhiteboardConstants;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;
import java.util.Set;


@Component(
        immediate = true, property = {
        JaxrsWhiteboardConstants.JAX_RS_APPLICATION_BASE + "=/generico",
        JaxrsWhiteboardConstants.JAX_RS_NAME + "=Generico.Rest",
        "auth.verifier.guest.allowed=true",
        "liferay.access.control.disable=true"
},
        service = Application.class)

public class SendEmailSalveListRestServiceApplication extends Application {

    public Set<Object> getSingletons() {
        return Collections.singleton(this);
    }

    @POST
    @Path("/salvar-lista-enviar-email")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response enviarEmailSavarNaListaDinamica(String payload) throws JSONException {

        boolean success = false;

        JSONObject object;
        object = JSONFactoryUtil.createJSONObject(payload);

        if (object == null || object.length() == 0) {
            _log.error(object);
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Bad Request: JSON vázio ou inválido.\"}").build();
        }

        try {

            long companyId = Long.parseLong(object.getString("companyId"));
            long groupId = Long.parseLong(object.getString("groupId"));
            long userId = Long.parseLong(object.getString("userId"));

            String recordSetName = object.getString("tabelaDinamica");

            if (recordSetName.isEmpty() || groupId == 0 || userId == 0 || companyId == 0){
                _log.error(object);
                return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\":\"Bad Request: Estão vázios alguns dos campos (recordSetName, companyId, groupId, userId).\"}").build();
            }

            DDLRecordSet recordSet = null;
            List<DDLRecordSet> recordSets = ddlRecordSetLocalService.getRecordSets(groupId);

            for (DDLRecordSet rs : recordSets){

                if (recordSetName.equals(rs.getNameCurrentValue())){
                    recordSet = rs;
                    success = _utilService.sendEmailAndSaveDynamicList(object);

                    if (success == false) {
                        _log.error(object);
                        return Response.status(Response.Status.BAD_REQUEST)
                                .entity("{\"error\":\"Bad Request: O envio de email não foi realizado.\"}").build();
                    }

                    success = _contentHelper.setDynamicDataListArray(object);
                    break;
                }

            }

            if (recordSet == null || success == false) {
                _log.error(object);
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\":\"Bad Request: Não foi salvo na lista.\"}").build();
            }

        }catch(Exception e){
            _log.error(object);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Internal Server Error: " + e.getMessage() + "\"}").build();
        }

        return Response.ok(object.toString(), MediaType.APPLICATION_JSON).build();
    }

    @Reference
    private DDLRecordSetLocalService ddlRecordSetLocalService;

    @Reference
    private ContentHelper _contentHelper;

    @Reference
    private UtilService _utilService;

    private final Log _log = LogFactoryUtil.getLog(SendEmailSalveListRestServiceApplication.class);

}

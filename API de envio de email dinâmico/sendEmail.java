package br.com.notrelabs.tasy.authenticator.application;

import br.com.gndi.util.service.UtilService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.jaxrs.whiteboard.JaxrsWhiteboardConstants;

import javax.ws.rs.*;
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
    @Path("/send-email")
    public Response enviarEmailDinamico(String payload) throws PortalException {

        boolean success = false;
        JSONObject object;
        object = JSONFactoryUtil.createJSONObject(payload);

        if (object == null || object.length() == 0) {
            _log.error(object);
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Bad Request: JSON vázio ou inválido.\"}").build();
        }

        try {

            success = _utilService.sendEmailAndSaveDynamicList(object);

            if (success == false) {
                throw new Exception("Erro ao enviar email");
            }

        }catch (Exception e){
            _log.error(e);
            return Response.status(Response.Status.NOT_FOUND).entity("{\"error\":\"Internal Server Error\"}"+e.toString()).build();
        }

        return Response.ok(object.toString(), MediaType.APPLICATION_JSON).build();
    }

   @Reference
   private UtilService _utilService;

}

package br.com.gndi.util.service;

import br.com.gndi.util.configuration.GndiUtilConfiguration;
import br.com.gndi.util.factory.MediaReplacedElementFactory;
import com.liferay.petra.content.ContentUtil;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.util.StringUtil;
import kong.unirest.Headers;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component(
        immediate = true,
        configurationPid = "br.com.gndi.util.configuration.GndiUtilConfiguration",
        service = UtilService.class
)
public class UtilServiceImpl implements UtilService {

    private volatile GndiUtilConfiguration _gndiUtilConfiguration;
    private String URL_ENVIAR_EMAIL = "/inst/v1/notificacao/e-mail/enviar";
    private String EMAIL_FROM = "portalgndi@intermedica.com.br";
    private String CHANNEL = "P";
    private String TEMPLATE_NAME = "TEMPLATE_DFA";
    private String TEMPLATE_TYPE = "HTML";

    @Override
	public boolean sendSubscriptionEmail(String emailTo, String internalEmail, String name, String cellphone, String cardNumber,
                                         String companyName, String subject, String body, String eventName, String eventCity, String eventDateTime, String eventVenue, String eventAddress, String eventDescription) {
        //recupera token
        String auth = this.getToken();

        if(auth != null) {
            byte[] anexo;
			try {
				anexo = this.generateEmail(name, emailTo, cellphone, cardNumber, companyName, eventName, eventCity, eventDateTime, eventVenue, eventAddress, eventDescription);
			    body = this.generateBody(name, cellphone);

	            //cria payload
	            String json = "{" +
                    "	\"channel\": \""+CHANNEL+"\"," +
                    "	\"from\": \""+URL_ENVIAR_EMAIL+"\"," +
                    "	\"to\": \""+internalEmail+"\"," +
                    "	\"subject\": \""+subject+"\"," +
                    "	\"templateName\": \""+TEMPLATE_NAME+"\"," +
                    "	\"templateType\": \""+TEMPLATE_TYPE+"\"," +
                    "	\"body\": \""+body+"\","+
                    "   \"parameters\": null ," +
                    "   \"files\": [{" +
                    "   \"name\": \"" + "Palestras e Cursos.pdf" + "\"," +
                    "   \"encodedFile\": \"" + Base64.getEncoder().encodeToString(anexo) + "\"," +
                    "   \"contentType\": \"application/pdf\"}]" +
                    "}";
	            
	            //chama servico
	            JSONObject retorno = this.callService(auth, URL_ENVIAR_EMAIL, json);

	            if(retorno != null ) {
	                return true;
	            }
			} catch (Exception e) {
				e.printStackTrace();
			}

        }
        return false;
    }

    @Override
    public boolean sendSubscriptionEmail(String emailTo, String internalEmail, String nomeCompleto, String telefone, String regiaoAtuacao, String subject) {

        String auth = this.getToken();

        if(auth != null) {
            String body;
            byte[] anexo;
            try {
                body = this.generateBodyQueroSerUmCorretor(emailTo, internalEmail, nomeCompleto, telefone, regiaoAtuacao, subject);

                //cria payload
                String json = "{" +
                        "	\"channel\": \""+CHANNEL+"\"," +
                        "	\"from\": \""+URL_ENVIAR_EMAIL+"\"," +
                        "	\"to\": \""+internalEmail+"\"," +
                        "	\"subject\": \""+subject+"\"," +
                        "	\"templateName\": \""+TEMPLATE_NAME+"\"," +
                        "	\"templateType\": \""+TEMPLATE_TYPE+"\"," +
                        "	\"body\": \""+body+"\","+
                        "   \"parameters\": null ," +
                        "   \"files\": [{" +
                        "   \"name\": \"" + "\"," +
                        "   \"encodedFile\": \"" + "\"," +
                        "   \"contentType\": \"application/json\"}]" +
                        "}";

                //chama servico
                JSONObject retorno = this.callService(auth, URL_ENVIAR_EMAIL, json);

                if(retorno != null ) {
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return false;
    }

    @Override
    public boolean sendSubscriptionEmailCorretorOdontoPF(String cro, String estado, String cidade, String telefone, String numero, String bairro, String cep, String internalEmail, String complemento, String rg, String anoDeFormacao, String especialidades, String cpf, String celular, String infoComplementar, String enderecoResidencial, String dataDeNascimento, String email, String conselho, String nomeCompleto) {
        //recupera token
        String auth = this.getToken();

        if(auth != null) {

            String body;
            String subject = "Credenciado Odonto PF";

            try {
                body = this.generateBodyQueroSerUmCorretorOdonto(cro, estado, cidade, telefone, numero, bairro, cep, complemento, rg, anoDeFormacao, especialidades, cpf, celular, infoComplementar, enderecoResidencial, dataDeNascimento, email, conselho, nomeCompleto);

                //cria payload
                String json = "{" +
                        "	\"channel\": \""+CHANNEL+"\"," +
                        "	\"from\": \""+URL_ENVIAR_EMAIL+"\"," +
                        "	\"to\": \""+internalEmail+"\"," +
                        "	\"subject\": \""+subject+"\"," +
                        "	\"templateName\": \""+TEMPLATE_NAME+"\"," +
                        "	\"templateType\": \""+TEMPLATE_TYPE+"\"," +
                        "	\"body\": \""+body+"\","+
                        "   \"parameters\": null ," +
                        "   \"files\": [{" +
                        "   \"name\": \"" + "\"," +
                        "   \"encodedFile\": \"" + "\"," +
                        "   \"contentType\": \"application/json\"}]" +
                        "}";

                //chama servico
                JSONObject retorno = this.callService(auth, URL_ENVIAR_EMAIL, json);

                if(retorno != null ) {
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return false;
    }

    @Override
    public boolean sendSubscriptionEmailCorretorOdontoPJ(String razaoSocial, String cnpj, String cro, String telefone, String celular, String cep, String enderecoComercial, String numero, String complemento, String bairro, String cidade, String estado, String regiao, String especialidades, String infoComplementar, String internalEmail) {
        //recupera token
        String auth = this.getToken();

        if(auth != null) {
            String body;
            String subject = "Credenciado Odonto PJ";

            try {
                body = this.generateBodyQueroSerUmCorretorOdontoPJ(razaoSocial, cnpj, cro, telefone, celular, cep, enderecoComercial, numero, complemento, bairro, cidade, estado, regiao, especialidades, infoComplementar);

                //cria payload
                String json = "{" +
                        "	\"channel\": \""+CHANNEL+"\"," +
                        "	\"from\": \""+URL_ENVIAR_EMAIL+"\"," +
                        "	\"to\": \""+internalEmail+"\"," +
                        "	\"subject\": \""+subject+"\"," +
                        "	\"templateName\": \""+TEMPLATE_NAME+"\"," +
                        "	\"templateType\": \""+TEMPLATE_TYPE+"\"," +
                        "	\"body\": \""+body+"\","+
                        "   \"parameters\": null ," +
                        "   \"files\": [{" +
                        "   \"name\": \"" + "\"," +
                        "   \"encodedFile\": \"" + "\"," +
                        "   \"contentType\": \"application/json\"}]" +
                        "}";

                //chama servico
                JSONObject retorno = this.callService(auth, URL_ENVIAR_EMAIL, json);

                if(retorno != null ) {
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return false;
    }

    @Override
    public boolean sendSubscriptionEmailCorretorSaudePF(String nomeCompleto, String tipoDeCredenciamento, String dataDeNascimento, String cpf, String rg, String cep, String enderecoResidencial, String numero, String complemento, String bairro, String estado, String cidade, String telefone, String celular, String infoComplemailementar, String conselho, String anoDeFormacao, String formacaoComplementar, String especialidades, String infoComplementar, String internalEmail) {
        //recupera token
        String auth = this.getToken();

        if(auth != null) {
            String body;
            String subject = "Credenciado Saúde PF";

            try {
                body = this.generateBodyQueroSerUmCorretorSaude(nomeCompleto, tipoDeCredenciamento, dataDeNascimento, cpf, rg, cep, enderecoResidencial, numero, complemento, bairro, estado, cidade, telefone, celular, infoComplemailementar, conselho, anoDeFormacao, formacaoComplementar, especialidades, infoComplementar);

                //cria payload
                String json = "{" +
                        "	\"channel\": \""+CHANNEL+"\"," +
                        "	\"from\": \""+URL_ENVIAR_EMAIL+"\"," +
                        "	\"to\": \""+internalEmail+"\"," +
                        "	\"subject\": \""+subject+"\"," +
                        "	\"templateName\": \""+TEMPLATE_NAME+"\"," +
                        "	\"templateType\": \""+TEMPLATE_TYPE+"\"," +
                        "	\"body\": \""+body+"\","+
                        "   \"parameters\": null ," +
                        "   \"files\": [{" +
                        "   \"name\": \"" + "\"," +
                        "   \"encodedFile\": \"" + "\"," +
                        "   \"contentType\": \"application/json\"}]" +
                        "}";

                //chama servico
                JSONObject retorno = this.callService(auth, URL_ENVIAR_EMAIL, json);

                if(retorno != null ) {
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return false;
    }

    @Override
    public boolean sendSubscriptionEmailCorretorSaudePJ(String razaoSocial, String cnpj, String crm, String telefone, String celular, String cep, String enderecoComercial, String numero, String complemento, String bairro, String cidade, String estado, String regiao, String especialidades, String infoComplementar, String internalEmail) {
        //recupera token
        String auth = this.getToken();

        if(auth != null) {
            String body;
            String subject = "Credenciado Saúde PJ";

            try {
                body = this.generateBodyQueroSerUmCorretorSaudePJ(razaoSocial, cnpj, crm, telefone, celular, cep, enderecoComercial, numero, complemento, bairro, cidade, estado, regiao, especialidades, infoComplementar);

                //cria payload
                String json = "{" +
                        "	\"channel\": \""+CHANNEL+"\"," +
                        "	\"from\": \""+URL_ENVIAR_EMAIL+"\"," +
                        "	\"to\": \""+internalEmail+"\"," +
                        "	\"subject\": \""+subject+"\"," +
                        "	\"templateName\": \""+TEMPLATE_NAME+"\"," +
                        "	\"templateType\": \""+TEMPLATE_TYPE+"\"," +
                        "	\"body\": \""+body+"\","+
                        "   \"parameters\": null ," +
                        "   \"files\": [{" +
                        "   \"name\": \"" + "\"," +
                        "   \"encodedFile\": \"" + "\"," +
                        "   \"contentType\": \"application/json\"}]" +
                        "}";

                //chama servico
                JSONObject retorno = this.callService(auth, URL_ENVIAR_EMAIL, json);

                if(retorno != null ) {
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return false;
    }

    @Override
    public boolean sendEmailAndSaveDynamicList(JSONObject object) {

        String auth = this.getToken();

        if (auth != null) {

            try {

                String body = this.bodyMount(object);

                // Cria a parte de arquivos
                StringBuilder filesJson = new StringBuilder();
                JSONObject arquivos = object.getJSONObject("arquivo");

                if (arquivos != null) {
                    filesJson.append("{")
                            .append("\"name\": \"").append(arquivos.getString("nomeArquivo")).append("\",")
                            .append("\"encodedFile\": \"").append(arquivos.getString("arquivoBase64")).append("\",")
                            // .append("\"contentType\": \"application/pdf\"")  // Supondo que todos são PDFs, ajustar se necessário
                            .append("\"contentType\": \"").append(arquivos.getString("tipoArquivo")).append("\"")
                            .append("}");

                    // for (String key : arquivos.keySet()) {
                    //     String encodedFile = Base64.getEncoder().encodeToString(arquivos.getString(key).getBytes());

                    //     filesJson.append("{")
                    //             .append("\"name\": \"").append(key).append("\",")
                    //             .append("\"encodedFile\": \"").append(encodedFile).append("\",")
                    //             .append("\"contentType\": \"application/pdf\"")  // Supondo que todos são PDFs, ajustar se necessário
                    //             .append("},");

                    // }
                    // Remove a última vírgula
                    // if (filesJson.length() > 0) {
                    //     filesJson.setLength(filesJson.length() - 1);
                    // }
                }

                // Cria o payload JSON
                String json = "{" +
                        "\"channel\": \"" + CHANNEL + "\"," +
                        "\"from\": \"" + EMAIL_FROM + "\"," +
                        "\"to\": \"" + object.getString("internalEmail") + "\"," +
                        "\"subject\": \"" + object.getString("subject") + "\"," +
                        "\"templateName\": \"" + object.getString("template") + "\"," +
                        "\"templateType\": \"" + TEMPLATE_TYPE + "\"," +
                        "\"body\": \"" + body + "\"," +
                        "\"parameters\": null," +
                        "\"files\": [" + filesJson + "]" +
                        "}";

                // Chama o serviço
                JSONObject retorno = this.callService(auth, URL_ENVIAR_EMAIL, json);

                if (retorno != null) {
                    return true;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return false;
    }

    private JSONObject callService(String auth, String path, String json) throws JSONException {

        String apiHost = _gndiUtilConfiguration.baseApiUrl();
        // Faz a chamada HTTP usando Unirest
        HttpResponse<String> response = Unirest.post(apiHost + path)
                .header("Content-Type", "application/json")
                .header("Authorization", auth)
                .body(json)  // Envia a string JSON
                .asString(); // Recebe a resposta como String

        // Verifica se a resposta foi bem-sucedida
        if (response.getStatus() == 200) {
            // Converte a resposta do corpo (string JSON) para JSONObject do Liferay
            return JSONFactoryUtil.createJSONObject(response.getBody());
        } else {
            // Se não for sucesso, retorna null ou você pode lançar uma exceção
            return null;
        }

    }

    private String getToken() {

        String apiHost = _gndiUtilConfiguration.baseApiUrl()  + "/inst/v1/autorizacao/token";
        String apiAuthBody = "{\"username\": \"" + _gndiUtilConfiguration.apiUsername() +
                          "\", \"password\": \"" + _gndiUtilConfiguration.apiPassword() + "\"}";

        HttpResponse<JsonNode> response = Unirest.post(apiHost)
                .header("Content-Type", "application/json")
                .body(apiAuthBody)
                .asJson();
        if(response.getStatus() == 200) {
            Headers headers = response.getHeaders();
            List<String> auth = headers.get("Authorization");
            return  auth.get(0);
        } else {
            return null;
        }
    }

    private byte[] generateEmail(String name, String email, String cellphone, String cardNumber, String companyName, String eventName, String eventCity, String eventDateTime, String eventVenue, String eventAddress, String eventDescription) throws Exception{
        String body = ContentUtil.get(UtilServiceImpl.class.getClassLoader(),  "templates/mail.html", false);
        
        body = StringUtil.replace(
                body,
                new String[] {
                        "[${NAME}]",
                        "[${EMAIL}]",
                        "[${CELLPHONE}]",
                        "[${CARDNUMBER}]",
                        "[${COMPANYNAME}]",
                        "[${EVENTNAME}]",
                        "[${EVENTCITY}]",
                        "[${EVENTDATETIME}]",
                        "[${EVENTVENUE}]",
                        "[${EVENTADDRESS}]",
                        "[${EVENTDESCRIPTION}]"},
                new String[] {
                        name,
                        email,
                        cellphone,
                        cardNumber,
                        companyName,
                        eventName,
                        eventCity,
                        eventDateTime,
                        eventVenue,
                        eventAddress,
                        eventDescription
                });
        
        ITextRenderer renderer = new ITextRenderer();
        SharedContext sharedContext = renderer.getSharedContext();
        sharedContext.setPrint(true);
        sharedContext.setInteractive(false);

        sharedContext.setReplacedElementFactory(new MediaReplacedElementFactory(renderer.getSharedContext().getReplacedElementFactory()));

        renderer.setDocumentFromString(body);


        renderer.layout();
        try (ByteArrayOutputStream fos = new ByteArrayOutputStream(body.length())) {
            renderer.createPDF(fos);
            return fos.toByteArray();
        }
    }

    private String generateBody(String name, String cellphone){
        String body = ContentUtil.get(UtilServiceImpl.class.getClassLoader(),  "templates/body.html", false);

        body = StringUtil.replace(
                body,
                new String[] {
                        "[${NAME}]",
                        "[${CELLPHONE}]"},
                new String[] {
                        name,
                        cellphone
                });

        body = body.replace("\"","\\\"");
        body = body.replace("\n", "");

        return body;
    }

    private String generateBodyQueroSerUmCorretor(String emailTo, String internalEmail,String nomeCompleto, String telefone, String regiaoAtuacao, String subject){

        String body = ContentUtil.get(UtilServiceImpl.class.getClassLoader(),  "templates/quero-ser-corretor.html", false);

        body = StringUtil.replace(
                body,
                new String[] {
                        "[${NOME_COMPLETO}]",
                        "[${EMAIL_TO}]",
                        "[${TELEFONE}]",
                        "[${REGIAO_ATUACAO}]"},
                new String[] {
                        nomeCompleto,
                        emailTo,
                        telefone,
                        regiaoAtuacao
                });

        body = body.replace("\"","\\\"");
        body = body.replace("\n", "");

        return body;
    }

    private String generateBodyQueroSerUmCorretorOdonto(String cro, String estado, String cidade, String telefone, String numero, String bairro, String cep, String complemento, String rg, String anoDeFormacao, String especialidades, String cpf, String celular, String infoComplementar, String enderecoResidencial, String dataDeNascimento, String email, String conselho, String nomeCompleto){

        String body = ContentUtil.get(UtilServiceImpl.class.getClassLoader(),  "templates/quero-ser-corretor-odonto.html", false);

        body = StringUtil.replace(
                body,
                new String[] {
                        "[${NOME_COMPLETO}]",
                        "[${CPF}]",
                        "[${RG}]",
                        "[${DATA_NASCIMENTO}]",
                        "[${CRO}]",
                        "[${CONSELHO}]",
                        "[${ANO_FORMACAO}]",
                        "[${EMAIL_TO}]",
                        "[${TELEFONE}]",
                        "[${CELULAR}]",
                        "[${CEP}]",
                        "[${ENDERECO}]",
                        "[${NUMERO}]",
                        "[${COMPLEMENTO}]",
                        "[${BAIRRO}]",
                        "[${CIDADE}]",
                        "[${ESTADO}]",
                        "[${ESPECIALIDADES}]",
                        "[${INFO_COMPLEMENTAR}]"},
                new String[] {
                        nomeCompleto,
                        cpf,
                        rg,
                        dataDeNascimento,
                        cro,
                        conselho,
                        anoDeFormacao,
                        email,
                        telefone,
                        celular,
                        cep,
                        enderecoResidencial,
                        numero,
                        complemento,
                        bairro,
                        cidade,
                        estado,
                        especialidades,
                        infoComplementar
                });

        body = body.replace("\"","\\\"");
        body = body.replace("\n", "");

        return body;
    }

    private String generateBodyQueroSerUmCorretorOdontoPJ(String razaoSocial, String cnpj, String cro, String telefone, String celular, String cep, String enderecoComercial, String numero, String complemento, String bairro, String cidade, String estado, String regiao, String especialidades, String infoComplementar){

        String body = ContentUtil.get(UtilServiceImpl.class.getClassLoader(),  "templates/quero-ser-corretor-odonto-pj.html", false);

        body = StringUtil.replace(
                body,
                new String[] {
                        "[${RAZAO_SOCIAL}]",
                        "[${CNPJ}]",
                        "[${CRO}]",
                        "[${TELEFONE}]",
                        "[${CELULAR}]",
                        "[${CEP}]",
                        "[${ENDERECO}]",
                        "[${NUMERO}]",
                        "[${COMPLEMENTO}]",
                        "[${BAIRRO}]",
                        "[${CIDADE}]",
                        "[${ESTADO}]",
                        "[${REGIAO}]",
                        "[${ESPECIALIDADES}]",
                        "[${INFO_COMPLEMENTAR}]"},
                new String[] {
                        razaoSocial,
                        cnpj,
                        cro,
                        telefone,
                        celular,
                        cep,
                        enderecoComercial,
                        numero,
                        complemento,
                        bairro,
                        cidade,
                        estado,
                        regiao,
                        especialidades,
                        infoComplementar
                });

        body = body.replace("\"","\\\"");
        body = body.replace("\n", "");

        return body;
    }

    private String bodyMount(JSONObject bodyString){

        // Carrega o template
        String body;

        if(bodyString.getString("template").contains(".html")) {
            body = ContentUtil.get(UtilServiceImpl.class.getClassLoader(), bodyString.getString("template"), false);
        } else {
            body = bodyString.getString("template");
        }

        // Cria um Map para armazenar as substituições
        Map<String, String> replacements = new HashMap<>();

        // Itera sobre as chaves do JSONObject e adiciona no Map
        for (String key : bodyString.keySet()) {
            String value = bodyString.getString(key);
            replacements.put(key, value);
        }

        // Itera sobre as entradas do mapa e substitui no template
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            String placeholder = "[${" + entry.getKey() + "}]";
            body = body.replace(placeholder, entry.getValue() != null ? entry.getValue() : "");
        }

        // Faz os ajustes necessários no body
        body = body.replace("\"", "\\\"");
        body = body.replace("\n", "");

        return body;
    }

    private String generateBodyQueroSerUmCorretorSaude(String nomeCompleto, String tipoDeCredenciamento, String dataDeNascimento, String cpf, String rg, String cep, String enderecoResidencial, String numero, String complemento, String bairro, String estado, String cidade, String telefone, String celular, String email, String conselho, String anoDeFormacao, String formacaoComplementar, String especialidades, String infoComplementar){

        String body = ContentUtil.get(UtilServiceImpl.class.getClassLoader(),  "templates/quero-ser-corretor-saude.html", false);

        body = StringUtil.replace(
                body,
                new String[] {
                        "[${NOME_COMPLETO}]",
                        "[${CPF}]",
                        "[${RG}]",
                        "[${DATA_NASCIMENTO}]",
                        "[${TIPO_DE_CREDENCIAMENTO}]",
                        "[${CONSELHO}]",
                        "[${ANO_FORMACAO}]",
                        "[${EMAIL_TO}]",
                        "[${TELEFONE}]",
                        "[${CELULAR}]",
                        "[${CEP}]",
                        "[${ENDERECO}]",
                        "[${NUMERO}]",
                        "[${COMPLEMENTO}]",
                        "[${BAIRRO}]",
                        "[${CIDADE}]",
                        "[${ESTADO}]",
                        "[${ESPECIALIDADES}]",
                        "[${FORMACAO_COMPLEMENTAR}]",
                        "[${INFO_COMPLEMENTAR}]"},
                new String[] {
                        nomeCompleto,
                        cpf,
                        rg,
                        dataDeNascimento,
                        tipoDeCredenciamento,
                        conselho,
                        anoDeFormacao,
                        email,
                        telefone,
                        celular,
                        cep,
                        enderecoResidencial,
                        numero,
                        complemento,
                        bairro,
                        cidade,
                        estado,
                        especialidades,
                        formacaoComplementar,
                        infoComplementar
                });

        body = body.replace("\"","\\\"");
        body = body.replace("\n", "");

        return body;
    }

    private String generateBodyQueroSerUmCorretorSaudePJ(String razaoSocial, String cnpj, String crm, String telefone, String celular, String cep, String enderecoComercial, String numero, String complemento, String bairro, String cidade, String estado, String regiao, String especialidades, String infoComplementar){

        String body = ContentUtil.get(UtilServiceImpl.class.getClassLoader(),  "templates/quero-ser-corretor-saude-pj.html", false);

        body = StringUtil.replace(
                body,
                new String[] {
                        "[${RAZAO_SOCIAL}]",
                        "[${CNPJ}]",
                        "[${CRM}]",
                        "[${TELEFONE}]",
                        "[${CELULAR}]",
                        "[${CEP}]",
                        "[${ENDERECO}]",
                        "[${NUMERO}]",
                        "[${COMPLEMENTO}]",
                        "[${BAIRRO}]",
                        "[${CIDADE}]",
                        "[${ESTADO}]",
                        "[${REGIAO}]",
                        "[${ESPECIALIDADES}]",
                        "[${INFO_COMPLEMENTAR}]"},
                new String[] {
                        razaoSocial,
                        cnpj,
                        crm,
                        telefone,
                        celular,
                        cep,
                        enderecoComercial,
                        numero,
                        complemento,
                        bairro,
                        cidade,
                        estado,
                        regiao,
                        especialidades,
                        infoComplementar
                });

        body = body.replace("\"","\\\"");
        body = body.replace("\n", "");

        return body;
    }


    @Activate
    @Modified
    protected void activate(Map<Object, Object> properties) {
        this._gndiUtilConfiguration = ConfigurableUtil.createConfigurable(GndiUtilConfiguration.class, properties);
    }
}

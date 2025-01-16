package br.com.gndi.util.content.helper;

import br.com.gndi.util.configuration.GndiUtilConfiguration;
import com.liferay.asset.kernel.model.AssetEntry;
import com.liferay.asset.kernel.service.AssetEntryLocalServiceUtil;
import com.liferay.document.library.kernel.service.DLAppLocalServiceUtil;
import com.liferay.document.library.util.DLURLHelperUtil;
import com.liferay.dynamic.data.lists.model.DDLRecord;
import com.liferay.dynamic.data.lists.model.DDLRecordSet;
import com.liferay.dynamic.data.lists.service.DDLRecordLocalService;
import com.liferay.dynamic.data.lists.service.DDLRecordSetLocalService;
import com.liferay.dynamic.data.mapping.model.*;
import com.liferay.dynamic.data.mapping.service.DDMStructureLocalServiceUtil;
import com.liferay.dynamic.data.mapping.service.DDMTemplateLocalServiceUtil;
import com.liferay.dynamic.data.mapping.storage.DDMFormFieldValue;
import com.liferay.dynamic.data.mapping.storage.DDMFormValues;
import com.liferay.dynamic.data.mapping.validator.DDMFormValuesValidationException;
import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.service.JournalArticleLocalServiceUtil;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.xml.*;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Éverton Lupi
 */
@Component(
	    immediate = true,
        configurationPid = "br.com.gndi.util.configuration.GndiUtilConfiguration",
	    service = ContentHelper.class
)
public class ContentHelper {

    private static final Log _log = LogFactoryUtil.getLog(ContentHelper.class);
    private static final org.apache.commons.logging.Log log = LogFactory.getLog(ContentHelper.class);

    @Reference
    private DDLRecordLocalService ddlRecordLocalService;
    @Reference
    private DDLRecordSetLocalService ddlRecordSetLocalService;

    private volatile GndiUtilConfiguration _gndiUtilConfiguration;

    @Activate
    @Modified
    protected void activate(Map<Object, Object> properties) {
        this._gndiUtilConfiguration = ConfigurableUtil.createConfigurable(GndiUtilConfiguration.class, properties);
    }

    public String getFirstCategory(long id) {
    	AssetEntry asset;
		try {
			asset = AssetEntryLocalServiceUtil.getEntry(JournalArticle.class.getName(), id);
			return asset.getCategories().size()>0?asset.getCategories().get(0).getName():"";
		} catch (PortalException e) {
		}
		return "";
    }

    public String getImageURL(String uuid, long groupId) {
    	try {
			FileEntry file = DLAppLocalServiceUtil.getFileEntryByUuidAndGroupId((String) uuid, groupId);
			return DLURLHelperUtil.getImagePreviewURL(file, file.getFileVersion(), null);
		} catch(Exception e) {
		}
		return "";
    }

    public String getStructureContentByName(String articleId, long groupId, String name)
            throws PortalException, DocumentException {
        _log.debug("Inicio metodo getStructureContentByName");
        final JournalArticle article = JournalArticleLocalServiceUtil.getArticle(groupId, articleId);
        final Document document = SAXReaderUtil.read(article.getContent());
        final Node node = document.selectSingleNode("/root/dynamic-element[@name='" + name + "']/dynamic-content");

        return node != null ? node.getText() : null;
    }

    public String formatDate(String data, String pattern)
            throws PortalException, DocumentException {
        _log.debug("Inicio metodo formatDate");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat sdf2 = new SimpleDateFormat(pattern, new Locale("pt", "BR"));
		try {
			return sdf2.format(sdf.parse(data));
		} catch (ParseException e) {
		}
        return "";
    }

    public String getDateByLocale(String articleId, long groupId, String name)
            throws PortalException, DocumentException {
        _log.debug("Inicio metodo getStructureContentByName");
        String dataPublicacao = getStructureContentByName(articleId, groupId, name);
        java.util.Calendar calendar = GregorianCalendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try {
			calendar.setTime(sdf.parse(dataPublicacao));
			return DateFormat.getDateInstance(DateFormat.LONG, new Locale("pt", "BR")).format(calendar.getTime());
		} catch (ParseException e) {
		}
        return "";
    }

    public String getGoogleSiteKey(ThemeDisplay themeDisplay) {
        _log.debug("Inicio metodo getKeyFromTheme");
        String googleSitekey = themeDisplay.getThemeSetting("Google Sitekey");
        if (Validator.isNull(googleSitekey)) {
            _log.debug("Chave nula no site, recuperando da propriedade geral");
            googleSitekey = _gndiUtilConfiguration.googleSiteKey();
        }
        return googleSitekey;
    }

    public List<String> getStructureContentByName(String articleId, long groupId, String nome, String nomePai)
            throws PortalException, DocumentException {
        _log.debug("Inicio metodo getStructureContentByName");
        final JournalArticle article = JournalArticleLocalServiceUtil.getArticle(groupId, articleId);
        final Document document = SAXReaderUtil.read(article.getContent());

        List<String> list = new ArrayList<String>();
        Element rootElement = document.getRootElement();
        List<Node> selectNodes = rootElement.selectNodes("/root/dynamic-element[@name='"+nomePai+"']");
    	for (Node node : selectNodes) {
    		Document documentNode = SAXReaderUtil.read(node.asXML());
    		Element rootCard = documentNode.getRootElement();
    		list.add(rootCard.valueOf("//dynamic-element[@name='"+nome+"']/dynamic-content/text()"));
		}
        return list;
    }

    public List<String> getchildrenByName(String articleId, long groupId, String nome, String nomePai)
            throws PortalException, DocumentException {
        _log.debug("Inicio metodo getStructureContentByName");
        final JournalArticle article = JournalArticleLocalServiceUtil.getArticle(groupId, articleId);
        final Document document = SAXReaderUtil.read(article.getContent());

        List<String> list = new ArrayList<String>();
        Element rootElement = document.getRootElement();
        List<Node> selectNodes = rootElement.selectNodes("/root/dynamic-element[@name='"+nomePai+"']");
        for (Node node : selectNodes) {
            Document documentNode = SAXReaderUtil.read(node.asXML());
            Element rootCard = documentNode.getRootElement();
            List<Node> childrenNodes = rootCard.selectNodes("//dynamic-element[@name='"+nome+"']/dynamic-content/text()");

            for (Node childrenode : childrenNodes) {
                list.add(childrenode.getText());
            }
        }
        return list;
    }

    public String getStructureContentByNameAndFather(String articleId, long groupId, String nome, String nomePai)
            throws PortalException, DocumentException {
        _log.debug("Inicio metodo getStructureContentByName");
        final JournalArticle article = JournalArticleLocalServiceUtil.getArticle(groupId, articleId);
        final Document document = SAXReaderUtil.read(article.getContent());

        Element rootElement = document.getRootElement();
        List<Node> selectNodes = rootElement.selectNodes("/root/dynamic-element[@name='"+nomePai+"']");
    	for (Node node : selectNodes) {
    		Document documentNode = SAXReaderUtil.read(node.asXML());
    		Element rootCard = documentNode.getRootElement();
    		return rootCard.valueOf("//dynamic-element[@name='"+nome+"']/dynamic-content/text()");
		}
        return "";
    }

    public String getStructureContentByNameLanguage(String articleId, long groupId, String name, String languageId)
            throws PortalException, DocumentException {
        _log.debug("Inicio metodo getStructureContentByName");
        final JournalArticle article = JournalArticleLocalServiceUtil.getArticle(groupId, articleId);
        final Document document = SAXReaderUtil.read(article.getContent());
        final Node node = document.selectSingleNode("/root/dynamic-element[@name='" + name + "']/dynamic-content[@language-id='" + languageId + "']");

        return node != null ? node.getText() : null;
    }

    public List<String> getDDMStructureContentsByName(String articleId, long groupId, String name)
            throws PortalException, DocumentException {
        final JournalArticle article = JournalArticleLocalServiceUtil.getArticle(groupId, articleId);
        final Document document = SAXReaderUtil.read(article.getContent());
        final List<Node> nodes = document.selectNodes("/root/dynamic-element[@name='" + name + "']/dynamic-content");

        return nodes.stream().map(Node::getText).collect(Collectors.toList());
    }

    public List<String> getStructureContentsByName(String articleId, long groupId, String name)
            throws PortalException, DocumentException {
        final JournalArticle article = JournalArticleLocalServiceUtil.getArticle(groupId, articleId);
        final Document document = SAXReaderUtil.read(article.getContent());
        final List<Node> nodes = document.selectNodes("/root/dynamic-element[@name='" + name + "']/dynamic-element");

        List<String> nodesContent = new ArrayList<String>();
        for (Node node : nodes) {
            String xml = node.asXML().replaceAll("\\n", "").replaceAll("\\t", "");
            nodesContent.add(xml.substring(xml.indexOf("[CDATA[") + 7, xml.indexOf("]]></dynamic-content></dynamic-element>")));
        }
        return nodesContent;
    }

    public DDMStructure getStructureById(long structureId) throws PortalException {
        return DDMStructureLocalServiceUtil.getStructure(structureId);
    }

    public DDMStructure getStructureByName(long groupId, String structureName) throws PortalException {
        final List<DDMStructure> ddmStructures = DDMStructureLocalServiceUtil.getStructures(groupId);

        return ddmStructures.stream().filter(s -> s.getName().contains(structureName)).findAny().orElse(null);
    }

    public List<JournalArticle> getContentsByStructureId(long groupId, long structureId, int start, int end)
            throws PortalException {
        final DDMStructure ddmStructure = getStructureById(structureId);
        return JournalArticleLocalServiceUtil.getArticlesByStructureId(groupId, ddmStructure.getStructureKey(), start,
                end, null);
    }

    public List<JournalArticle> getContentsByStructureId(long groupId, long structureId) throws PortalException {
        return getContentsByStructureId(groupId, structureId, -1, -1);
    }

    public DDMTemplate getTemplateByName(long classPK, String name) {
        final List<DDMTemplate> ddmTemplates = DDMTemplateLocalServiceUtil.getTemplates(classPK);

        return ddmTemplates.stream().filter(s -> s.getName().contains(name)).findAny().orElse(null);
    }

    public DDMTemplate getTemplateById(long modelId) throws PortalException {
        return DDMTemplateLocalServiceUtil.getTemplate(modelId);
    }

    public String[] getDynamicDataListArray(String listName, long groupId) {

        try {
            DDLRecordSet recordSet = null;
            List<DDLRecordSet> recordSets = ddlRecordSetLocalService.getRecordSets(groupId);
            for (DDLRecordSet rs : recordSets) {
                if (listName.equals(rs.getNameCurrentValue())) {
                    recordSet = rs;
                }
            }
            if (recordSet == null) {
                throw new Exception("No record set found with the name: " + listName);
            }
            List<DDLRecord> records = ddlRecordLocalService.getRecords(recordSet.getRecordSetId());
            String[] array = new String[records.size()];
            int index = 0;

            for (DDLRecord record : records) {
                DDMFormValues ddmFormValues = record.getDDMFormValues();
                List<DDMFormFieldValue> ddmFormFieldValues = ddmFormValues.getDDMFormFieldValues();
                for (DDMFormFieldValue ddmFormFieldValue : ddmFormFieldValues) {
                    String fieldName = ddmFormFieldValue.getName();
                    if (fieldName.contains("Região")) {
                        String fieldValue = ddmFormFieldValue.getValue().getString(LocaleUtil.BRAZIL);
                        array[index] = fieldValue;
                        index++;
                    }
                }
            }
            return array;
        } catch (Exception e) {
            // Handle the exception as needed
        }
        return null;
    }
    

    public JSONArray getDynamicDataListJson(String listName, long groupId) {

        try {
            DDLRecordSet recordSet = null;
            List<DDLRecordSet> recordSets = ddlRecordSetLocalService.getRecordSets(groupId);
            
            // Encontrar o conjunto de registros correspondente ao nome fornecido
            for (DDLRecordSet rs : recordSets) {
                if (listName.equals(rs.getNameCurrentValue())) {
                    recordSet = rs;
                    break;
                }
            }
    
            if (recordSet == null) {
                throw new Exception("No record set found with the name: " + listName);
            }
    
            List<DDLRecord> records = ddlRecordLocalService.getRecords(recordSet.getRecordSetId());
    
            // Criar o array JSON que vai armazenar os registros
            JSONArray jsonArray = JSONFactoryUtil.createJSONArray();
    
            for (DDLRecord record : records) {
                DDMFormValues ddmFormValues = record.getDDMFormValues();
                List<DDMFormFieldValue> ddmFormFieldValues = ddmFormValues.getDDMFormFieldValues();
    
                // Criar um objeto JSON para armazenar os valores do registro atual
                JSONObject recordJson = JSONFactoryUtil.createJSONObject();
    
                for (DDMFormFieldValue ddmFormFieldValue : ddmFormFieldValues) {
                    String fieldName = ddmFormFieldValue.getName();
                    String fieldValue = ddmFormFieldValue.getValue().getString(LocaleUtil.BRAZIL);
    
                    // Adiciona cada campo ao objeto JSON do registro
                    recordJson.put(fieldName, fieldValue);
                }
    
                // Adicionar o objeto do registro ao array JSON
                jsonArray.put(recordJson);
            }
    
            return jsonArray;
        } catch (Exception e) {
            // Lidar com exceções conforme necessário
            JSONArray errorArray = JSONFactoryUtil.createJSONArray();
            errorArray.put(JSONFactoryUtil.createJSONObject().put("error", e.getMessage()));
            return errorArray;
        }
    }

    public String[] getEspecialidades(String listName, long groupId) {

        try {
            DDLRecordSet recordSet = null;
            List<DDLRecordSet> recordSets = ddlRecordSetLocalService.getRecordSets(groupId);
            for (DDLRecordSet rs : recordSets) {
                if (listName.equals(rs.getNameCurrentValue())) {
                    recordSet = rs;
                }
            }
            if (recordSet == null) {
                throw new Exception("No record set found with the name: " + listName);
            }
            List<DDLRecord> records = ddlRecordLocalService.getRecords(recordSet.getRecordSetId());
            String[] array = new String[records.size()];
            int index = 0;

            for (DDLRecord record : records) {
                DDMFormValues ddmFormValues = record.getDDMFormValues();
                List<DDMFormFieldValue> ddmFormFieldValues = ddmFormValues.getDDMFormFieldValues();
                for (DDMFormFieldValue ddmFormFieldValue : ddmFormFieldValues) {
                    String fieldName = ddmFormFieldValue.getName();
                    if (fieldName.contains("especialidade")) {
                        String fieldValue = ddmFormFieldValue.getValue().getString(LocaleUtil.BRAZIL);
                        array[index] = fieldValue;
                        index++;
                    }
                }
            }
            return array;
        } catch (Exception e) {
            // Handle the exception as needed
        }
        return null;
    }

    public String[] getTiposDeCredenciamento(String listName, long groupId) {

        try {
            DDLRecordSet recordSet = null;
            List<DDLRecordSet> recordSets = ddlRecordSetLocalService.getRecordSets(groupId);
            for (DDLRecordSet rs : recordSets) {
                if (listName.equals(rs.getNameCurrentValue())) {
                    recordSet = rs;
                }
            }
            if (recordSet == null) {
                throw new Exception("No record set found with the name: " + listName);
            }
            List<DDLRecord> records = ddlRecordLocalService.getRecords(recordSet.getRecordSetId());
            String[] array = new String[records.size()];
            int index = 0;

            for (DDLRecord record : records) {
                DDMFormValues ddmFormValues = record.getDDMFormValues();
                List<DDMFormFieldValue> ddmFormFieldValues = ddmFormValues.getDDMFormFieldValues();
                for (DDMFormFieldValue ddmFormFieldValue : ddmFormFieldValues) {
                    String fieldName = ddmFormFieldValue.getName();
                    if (fieldName.contains("credenciamento")) {
                        String fieldValue = ddmFormFieldValue.getValue().getString(LocaleUtil.BRAZIL);
                        array[index] = fieldValue;
                        index++;
                    }
                }
            }
            return array;
        } catch (Exception e) {
            // Handle the exception as needed
        }
        return null;
    }

    public boolean setDynamicDataListArray(JSONObject object) {
        try {
            String recordSetName = object.getString("tabelaDinamica");
            long groupId = Long.parseLong(object.getString("groupId"));
            long userId = Long.parseLong(object.getString("userId"));
            long companyId = Long.parseLong(object.getString("companyId"));

            // Criar um ServiceContext básico
            ServiceContext serviceContext = new ServiceContext();
            serviceContext.setScopeGroupId(groupId);
            serviceContext.setCompanyId(companyId);
            serviceContext.setUserId(userId);

            // Buscar a lista dinâmica pelo nome
            List<DDLRecordSet> recordSets = ddlRecordSetLocalService.getRecordSets(groupId);
            DDLRecordSet targetRecordSet = null;

            for (DDLRecordSet recordSet : recordSets) {
                if (recordSet.getNameCurrentValue().equals(recordSetName)) {
                    targetRecordSet = recordSet;
                    break;
                }
            }

            if (targetRecordSet == null) {
                throw new NoSuchElementException("Lista Dinâmica não encontrada: " + recordSetName);
            }

            // Obter a estrutura e o formulário
            DDMStructure ddmStructure = targetRecordSet.getDDMStructure();
            DDMForm ddmForm = ddmStructure.getDDMForm();
            List<DDMFormField> ddmFormFields = ddmForm.getDDMFormFields();

            // Criar DDMFormValues
            DDMFormValues ddmFormValues = new DDMFormValues(ddmForm);
            ddmFormValues.setAvailableLocales(Collections.singleton(LocaleUtil.getSiteDefault()));
            ddmFormValues.setDefaultLocale(LocaleUtil.getSiteDefault());

            // Criar e adicionar valores aos campos do formulário
            for (DDMFormField ddmFormField : ddmFormFields) {
                String fieldName = ddmFormField.getName();
                String fieldValue = object.getString(fieldName);

                // Verifique se o campo é obrigatório e tem valor válido
                if (Validator.isNotNull(fieldValue)) {
                    DDMFormFieldValue ddmFormFieldValue = new DDMFormFieldValue();
                    ddmFormFieldValue.setName(fieldName);
                    Value value = new LocalizedValue();
                    value.addString(Locale.forLanguageTag("pt-BR"), fieldValue);
                    ddmFormFieldValue.setValue(value);
                    ddmFormValues.addDDMFormFieldValue(ddmFormFieldValue);
                } else {
                    log.error("Campo " + fieldName + " não recebeu um valor válido.");
                }
            }

            // Adicionar o registro à lista dinâmica
            ddlRecordLocalService.addRecord(
                    userId,                            // ID do usuário
                    groupId,                           // ID do grupo
                    targetRecordSet.getRecordSetId(),  // ID do conjunto de registros
                    0,                                 // Índice de exibição (displayIndex)
                    ddmFormValues,                     // Valores do formulário DDM
                    serviceContext                     // Contexto do serviço
            );

            return true;
        } catch (DDMFormValuesValidationException e) {
            log.error("Erro de validação para o campo " + e.getMessage(), e);
            return false;
        } catch (Exception e) {
            log.error("Erro ao gravar dados na lista dinâmica: " + e.getMessage(), e);
            return false;
        }
    }

}

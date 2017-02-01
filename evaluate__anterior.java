public void evaluate(MbMessageAssembly inAssembly) throws MbException {
		MbOutputTerminal out = getOutputTerminal("out");
		
		MbMessage inMessage = inAssembly.getMessage();
		MbMessageAssembly outAssembly = null;
		 
		try {
			
			MbMessage newMessage = new MbMessage();
	        MbElement outRoot = newMessage.getRootElement();
	        MbElement xmlnscOut = outRoot.createElementAsLastChild(Constantes.DOMAIN_XMLNSC);
	        MbElement loggJava = inAssembly.getGlobalEnvironment().getRootElement();
	        MbElement mbVariables = UtilJava.getIdElement(loggJava, Constantes.VARIABLES);
	        MbElement mbContext = UtilJava.getIdElement(mbVariables, Constantes.CONTEXT);	        
	        
	        MbElement mbPathProperLog4j = UtilJava.getIdElement(mbContext, "UDP_PATH_PROPERTIES_LOG4J");
	        MbElement mbPathProperJasper = UtilJava.getIdElement(mbContext, Constantes.UDP_PATH_PROPERTIES_JASPER);
	        
		
			MbElement root = inMessage.getRootElement();
			MbElement variables = root.getFirstElementByPath("/SOAP");
			MbElement xmlnsc = variables.getFirstChild().getFirstChild();
			MbElement firma = root.getLastChild();
			
			Map mapGeneral = new HashMap<>();
			Map mapListaParametros = new HashMap<>();
			
		
			if (firma.getFirstChild() != null) {
				MbElement eleIM = firma.getFirstChild();
				ByteArrayInputStream bais = new ByteArrayInputStream((byte[]) eleIM.getValue());
				mapGeneral.put("firma", bais);
				
				eleIM = eleIM.getNextSibling()!=null?eleIM.getNextSibling():null;
			
			}
	    
			MbElement elemModelo = UtilJava.getIdElement(xmlnsc, Constantes.CARGO_ID_MODELO);
			mapListaParametros.put(elemModelo.getName().toString(), elemModelo.getValueAsString());
			 
			//Penultima posicion
			MbElement elemListaGrupo = UtilJava.getIdElement(xmlnsc, Constantes.CARGO_ID_LISTA_GRUPOS);
			
			//Ultima posicion
			//<listaParrafosDinamicos>...</listaParrafosDinamicos>
			MbElement elemHeaderParrafosDina = UtilJava.getIdElement(xmlnsc, Constantes.CARGO_ID_LISTA_PARRAFOS_DINAMICOS);
			
			//Lista e Grupos (grupo,grupo,grupo,grupo,grupo,....grupo,)
			MbElement elemGrupos = UtilJava.getIdElement(elemListaGrupo, Constantes.ID_GRUPOS);
			
			/************* Variables para el uso de TABLAS ****************/
			//Mapeo q se usará para los de tipo "TA"(TABLA)
			Map<String, String> myMapFila = new HashMap<>();
			//Listas genericas para poder llenar los campos y direccionarlos al MapGeneral
			List auxList = new ArrayList();
			/**************************************************************/
			
			boolean indAgregarListaToMap = false;
			
			while (elemGrupos != null){
				//-------------------------------------<listaMarcas> --- 
				MbElement elemMarca = UtilJava.getIdElement(elemGrupos, Constantes.CARGO_LISTA_MARCAS).getFirstChild();
				
				while (elemMarca!= null) {
					//------------------------------------------<marca> -------- <tagText>
					MbElement eleMarca_idMarca = UtilJava.getIdElement(elemMarca, Constantes.CARGO_MARCA_ID_MARCA);
					MbElement eleTipoMarca = UtilJava.getIdElement(elemMarca, Constantes.CARGO_MARCA_ID_TIPO_MARCA);
					//Valores de la primera Lista, (Fila)
					
					/**  Cuando el TIPO de Marca es "SE" (SELECT): Se va a mapear directamente en MapGeneral ya que
					 * 	  estos datos son unicos en el documento y se reutiliza en todo el informe
					 * Demo:
					 * (0x03000100:Attribute):tipoMarca                       = 'SE' (CHARACTER)
		                (0x01000000:Folder   )http://onp.gob.pe/schema:tagText = (
		                  (0x03000100:Attribute  ):help                             = 'Sexo del apoderado' (CHARACTER)
		                  (0x03000100:Attribute  ):idTag                            = 'FE_NACI' (CHARACTER)
		                  (0x03000100:Attribute  ):visibility                       = 0 (INTEGER)
		                  (0x03000000:PCDataField)http://onp.gob.pe/schema:tagValue = '' (CHARACTER)
		                )
					 * */
					
					if (eleTipoMarca.getValueAsString().equals(Constantes.SELECT)) {
						MbElement eleListaTagText = eleTipoMarca.getNextSibling();
						while (eleListaTagText != null) {
							// ------tagValue-------- 
							//'getIdTagText' Metodo que busca el elemento que contenga el ID del TagText
							MbElement _getId = UtilJava.getIdElement(eleListaTagText, Constantes.CARGO_TIPO_SELECT_TABLA_ID_TAG_TEXT);
							//-----------------------------------------------
							
							//Se obtiene el valor del 'tagValue'
							String value = eleListaTagText.getLastChild().getValueAsString();
							//--------------------------------
							
							mapGeneral.put(eleMarca_idMarca.getValueAsString()+":"+_getId.getValueAsString(), value);
							eleListaTagText = eleListaTagText.getNextSibling()!=null?eleListaTagText.getNextSibling():null;
							
						}
					}
					
					/**
					 *  Cuando el TIPO de Marca es "TA" (TABLA): Se realiza el mapeo de una lista insertandolo en MapGeneral
					 * Demo
					 *
						(0x03000100:Attribute):tipoMarca                        = 'TA' (CHARACTER)
						(0x01000000:Folder   )http://onp.gob.pe/schema:tagTable = (
		                  (0x03000100:Attribute):idTag                                  = 'GEVP001_SOLICITANTE_DOCS_PERS' (CHARACTER)
		                  (0x03000100:Attribute):selectionType                          = 0 (INTEGER)
		                  (0x01000000:Folder   )http://onp.gob.pe/schema:tagTableHeader = (
		                    (0x01000000:Folder)http://onp.gob.pe/schema:tagText = (
		                      (0x03000100:Attribute):help       = 'Forma de presentación' (CHARACTER)
		                      (0x03000100:Attribute):idTag      = 'DE_FORM_PRES' (CHARACTER)
		                      (0x03000100:Attribute):label      = '' (CHARACTER)
		                      (0x03000100:Attribute):visibility = 0 (INTEGER)
		                    )
		                  )
		                  (0x01000000:Folder   )http://onp.gob.pe/schema:tagTableBody   = (
		                    (0x01000000:Folder)http://onp.gob.pe/schema:tagTableRow = (
		                      (0x03000000:PCDataField)http://onp.gob.pe/schema:tagValue = 'FORM_PRES_EXHI' (CHARACTER)
		                    )
		                  )
		                )
					 * 
					 * */
					else if(eleTipoMarca.getValueAsString().equals(Constantes.TABLA)) {
						MbElement elemTagTableHeader = UtilJava.getIdElement(eleTipoMarca.getNextSibling(),Constantes.CARGO_TABLA_HEADER);
						//tagTableHeader
						if (elemTagTableHeader != null) {
							// ------tagText Id-------- 
							MbElement elemTagText = elemTagTableHeader.getFirstChild();
							
							MbElement elemTagTableBody = elemTagTableHeader.getNextSibling();
							int contadorFolios = 0;
							// tagTableBody
							if (elemTagTableBody != null) {
								MbElement elemTagTableRow = elemTagTableBody.getFirstChild();
								auxList = new ArrayList();
								indAgregarListaToMap = false;
								while (elemTagTableRow != null) {
									MbElement elementTagValue = elemTagTableRow.getFirstChild();
									myMapFila= new HashMap<>();
									while (elementTagValue != null) {
										//Se obtiene  idTag
										//'getIdTagText' Metodo que busca el elemento que contenga el ID del TagText 
										MbElement _getIdTA = UtilJava.getIdElement(elemTagText, Constantes.CARGO_TIPO_SELECT_TABLA_ID_TAG_TEXT);
										
										//Setear Valor de Fecha
										String value = UtilJava.validacionElementValue(elementTagValue.getValueAsString(), _getIdTA.getValueAsString());
										
										//Contador de Numero de Folios
										if (_getIdTA.getValueAsString().equals(Constantes.NU_FOLI)) {
											contadorFolios = contadorFolios + Integer.parseInt(value);
										}
										
										if (value != "" || value != null) {
											myMapFila.put(_getIdTA.getValueAsString(), value);
											indAgregarListaToMap = true;
										}else {
											indAgregarListaToMap=false;
										}
										
										elemTagText = elemTagText.getNextSibling()!=null?elemTagText.getNextSibling():null;
										elementTagValue = elementTagValue.getNextSibling()!=null?elementTagValue.getNextSibling():null;
										
									}
									//Se añade el Map a la lista
									auxList.add(myMapFila);
									
									//Se hace un condicional si es que hay mas filas de tipo Row en el XML
									elemTagTableRow = elemTagTableRow.getNextSibling()!=null?elemTagTableRow.getNextSibling():null;
									elemTagText = elemTagTableHeader.getFirstChild();
									}
									
									if (elemModelo.getValueAsString().equals(Constantes.ACRE_001)||
										elemModelo.getValueAsString().equals(Constantes.ACEV_041)){
										Method.setearCamposACRE_001(eleMarca_idMarca, mapGeneral, auxList);
									}
									
									//Cuerpo de Desarrollo de GEVP-010
									if (elemModelo.getValueAsString().equals(Constantes.GEVP_010)||
										elemModelo.getValueAsString().equals(Constantes.GEVP_011)||
										elemModelo.getValueAsString().equals(Constantes.GEVP_012)){
										if (eleMarca_idMarca.getValueAsString().equals(Constantes.GEVP010_LIQU_DEVE_CALC_CONC) ) {
											if (indAgregarListaToMap) {
												Method.aniadirObjetoAMarca(mapGeneral, auxList, Constantes.GEVP010_LIQU_DEVE_CALC_PERI, Constantes.GEVP010_LIQU_DEVE_CALC_CONC);
											}
											
										}else if (eleMarca_idMarca.getValueAsString().equals(Constantes.GEVP010_LIQU_COBR_CALC_CONC) ) {
											if (indAgregarListaToMap) {
												Method.aniadirObjetoAMarcaCobrado(mapGeneral, auxList, Constantes.GEVP010_LIQU_COBR_CALC_PERI, Constantes.GEVP010_LIQU_COBR_CALC_CONC);
											}
											
										}
									}
									
									//Se agrega todas las variables que se mostrarán en el Reporte, Solo Listas
									/*if (!indAgregarListaToMap) {
										
									}else {
										indAgregarListaToMap = false;
									}*/
									mapGeneral.put(eleMarca_idMarca.getValueAsString(), auxList);
									auxList = new ArrayList();
									
							}
							mapGeneral.put(eleMarca_idMarca.getValueAsString() + ":NU_FOLI_TOTAL", contadorFolios + "");
						}
					}
					//misListas.add(myMapFila);value
					elemMarca = elemMarca.getNextSibling()!=null?elemMarca.getNextSibling():null;
					/***Termina el obtener de la fila de una lista**/
				}
				//Nuestra Lista
				//mapGeneral.put(elemMarca.getFirstChild().getValueAsString(), misListas);
				elemGrupos = elemGrupos.getNextSibling()!=null?elemGrupos.getNextSibling():null;
			}
			
			// <parrafoDinamico>.....</parrafoDinamico>
			MbElement eleParrafosDinamicos = UtilJava.getIdElement(elemHeaderParrafosDina, Constantes.PARRAFOS_DINAMICOS);
			
			/** Se enviará un atriburo de tipo Map para poder 
			 *  hacer el juego de visualizacion de los parametros
			 **/
			
			while (eleParrafosDinamicos!= null ) {
				String id = UtilJava.getIdElement(eleParrafosDinamicos, Constantes.CARGO_ID_PARRAFO_DINAMICO).getValueAsString();
				String value  = UtilJava.getIdElement(eleParrafosDinamicos, Constantes.CARGO_ID_INDICADOR_GENERACION).getValueAsString();
				mapListaParametros.put(id, value.toLowerCase());
				eleParrafosDinamicos = eleParrafosDinamicos.getNextSibling()!=null?eleParrafosDinamicos.getNextSibling():null;
			}
			
			Collection<Map> myDataSource = new ArrayList<Map>(); 
			myDataSource.add(mapGeneral); 
			
			//Registro entrada LIB_JASPER
			mbVariables.createElementAsLastChild(MbElement.TYPE_NAME, "JASPER_IN_mapListaParametros", mapListaParametros.toString());
			mbVariables.createElementAsLastChild(MbElement.TYPE_NAME, "JASPER_IN_myDataSource", myDataSource.toString());
			
			byte[] myBytes = ParametriaCargoRegistro.retornarBlobReporte(mapListaParametros, myDataSource, mbPathProperJasper.getValueAsString(),mbPathProperLog4j.getValueAsString());
			
			//Nuevo mensaje		
			MbElement outData = xmlnscOut.createElementAsLastChild(MbElement.TYPE_NAME, "Data", null);
			MbElement outBlob = outData.createElementAsLastChild(MbBLOB.PARSER_NAME);
			outBlob.setName("BLOB"); 
			outBlob.createElementAsLastChild(MbElement.TYPE_NAME_VALUE,"BLOB", myBytes);
			
			//Generacion RTF
			/*MbElement outBytesBase4 = xmlnscOut.createElementAsLastChild(MbElement.TYPE_NAME, "DataBase64", null);
			String encodedText = new String(Base64.encode(myBytes));
			outBytesBase4.createElementAsLastChild(MbElement.TYPE_NAME_VALUE,"BytesBase64",encodedText);
			*/
	        outAssembly = new MbMessageAssembly(inAssembly,newMessage);
	        
	        
			// End of user code
			// ----------------------------------------------------------
		} catch (MbException e) {
			// Re-throw to allow Broker handling of MbException
			throw new MbUserException(this, "evaluate()", "", "", e.toString(),
					null); 
		} catch (RuntimeException e) {
			// Re-throw to allow Broker handling of RuntimeException
			throw new MbUserException(this, "evaluate()", "", "", e.toString(),
					null);
		} catch (Exception e) {
			// Consider replacing Exception with type(s) thrown by user code
			// Example handling ensures all exceptions are re-thrown to be handled in the flow
			throw new MbUserException(this, "evaluate()", "", "", e.toString(),
					null);
		}
		// The following should only be changed
		// if not propagating message to the 'out' terminal
		
		out.propagate(outAssembly,true);
	}
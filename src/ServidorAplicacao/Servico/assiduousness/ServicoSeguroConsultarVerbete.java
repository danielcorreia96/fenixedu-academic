package ServidorAplicacao.Servico.assiduousness;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.ListIterator;
import java.util.Locale;

import Dominio.Horario;
import Dominio.HorarioTipo;
import Dominio.IStrategyHorarios;
import Dominio.IStrategyJustificacoes;
import Dominio.Justificacao;
import Dominio.MarcacaoPonto;
import Dominio.ParamJustificacao;
import Dominio.StatusAssiduidade;
import Dominio.SuporteStrategyHorarios;
import Dominio.SuporteStrategyJustificacoes;
import ServidorAplicacao.ServicoAutorizacao;
import ServidorAplicacao.ServicoSeguro;
import ServidorAplicacao.Servico.exceptions.NotExecuteException;
import ServidorPersistenteJDBC.IFeriadoPersistente;
import ServidorPersistenteJDBC.SuportePersistente;
import Util.Comparador;
import Util.FormataCalendar;
import constants.assiduousness.Constants;

/**
 *
 * @author  Fernanda Quit�rio & Tania Pous�o
 */
public class ServicoSeguroConsultarVerbete extends ServicoSeguro {

	private Integer _numMecanografico;
	private Timestamp _dataInicioEscolha = null;
	private Timestamp _dataFimEscolha = null;
	private Locale _locale = null;

	private ArrayList _listaFuncionarios = new ArrayList();
	private ArrayList _listaEstados = new ArrayList();
	private ArrayList _listaCartoes = null;
	private ArrayList _listaRegimes = null;
	private ArrayList _listaMarcacoesPonto = null;
	private ArrayList _listaSaldos = new ArrayList();
	private ArrayList _listaJustificacoes = null;
	private ArrayList _listaParamJustificacoes = null;

	private Horario _horario = null;
	private HorarioTipo _horarioTipo = null;

	private long _saldoPrimEscalao = 0;
	private long _saldoSegEscalao = 0;
	private long _saldoDepoisSegEscalao = 0;
	private long _saldoNegativo = 0;
	private long _saldoNocturno = 0;
	private long _saldoNoctPrimEscalao = 0;
	private long _saldoNoctSegEscalao = 0;
	private long _saldoNoctDepoisSegEscalao = 0;
	private long _saldoHNFinal = 0;
	private long _saldoPFFinal = 0;
	private long _saldoDSC = 0;
	private long _saldoDS = 0;

	private Timestamp _dataConsulta = null;
	private Calendar _calendarioConsulta = null;
	private Timestamp _dataInicio = null;
	private Timestamp _dataFim = null;
	private Date _dataAssiduidade = null;
	private boolean _mesInjustificado = false;
	private boolean _diasInjustificados = false;
	private boolean _statusPendente = false;

	private ArrayList _listaVerbeteDiaria = new ArrayList();
	private ArrayList _listaVerbeteBody = new ArrayList();

	public ServicoSeguroConsultarVerbete(
		ServicoAutorizacao servicoAutorizacaoLer,
		Integer numMecanografico,
		Timestamp dataInicioEscolha,
		Timestamp dataFimEscolha,
		Locale locale) {
		super(servicoAutorizacaoLer);
		_numMecanografico = numMecanografico;
		_dataInicioEscolha = dataInicioEscolha;
		_dataFimEscolha = dataFimEscolha;
		_locale = locale;

		_dataConsulta = new Timestamp(_dataInicioEscolha.getTime());
	}

	public void execute() throws NotExecuteException {
		try {
			lerStatusAssiduidade();
		} catch (NotExecuteException nee) {
			throw new NotExecuteException(nee.getMessage());
		}

		try {
			lerFimAssiduidade();
		} catch (NotExecuteException nee) {
			throw new NotExecuteException(nee.getMessage());
		}

		try {
			lerInicioAssiduidade();
		} catch (NotExecuteException nee) {
			throw new NotExecuteException(nee.getMessage());
		}

		try {
			construirEscolhasMarcacoesPonto();
		} catch (NotExecuteException nee) {
			throw new NotExecuteException(nee.getMessage());
		}

		try {
			verificarMesInjustificado();
		} catch (NotExecuteException nee) {
			throw new NotExecuteException(nee.getMessage());
		}

		if (!_mesInjustificado) {
			Calendar agora = Calendar.getInstance();
			agora.set(Calendar.HOUR_OF_DAY, 00);
			agora.set(Calendar.MINUTE, 00);
			agora.set(Calendar.SECOND, 00);
			agora.set(Calendar.MILLISECOND, 00);
			Timestamp agoraTimestamp = new Timestamp(agora.getTimeInMillis());

			_calendarioConsulta = Calendar.getInstance();
			_calendarioConsulta.setLenient(false);
			Calendar calendario = Calendar.getInstance();
			calendario.setLenient(false);
			//==================================== Inicio da construcao da lista a mostrar no jsp =======================================
			// comeca pela data final para que apareca por ordem crescente de datas no jsp
			while (_dataConsulta.before((Timestamp) _dataFimEscolha)) {
				calendario.clear();
				calendario.setTimeInMillis(_dataConsulta.getTime());

				// consulta do horario do funcionario numa determinada data
				try {
					lerHorario();
				} catch (NotExecuteException nee) {
					throw new NotExecuteException(nee.getMessage());
				}

				try {
					verificarStatusAssiduidade();
				} catch (NotExecuteException nee) {
					throw new NotExecuteException(nee.getMessage());
				}

				if (_statusPendente) {
					_statusPendente = false;
					continue;
				}

				try {
					verificarDiasInjustificados(calendario);
				} catch (NotExecuteException nee) {
					throw new NotExecuteException(nee.getMessage());
				}

				if (_diasInjustificados) {
					_diasInjustificados = false;
					continue;
				}

				// consulta das marcacoes do funcionario neste dia, tendo atencao ao expediente deste horario	
				calcularIntervaloConsulta();

				try {
					consultarMarcacoesPonto();
				} catch (NotExecuteException nee) {
					throw new NotExecuteException(nee.getMessage());
				}

				_listaVerbeteDiaria.add(0, FormataCalendar.data(calendario));
				_listaVerbeteDiaria.add(1, "&nbsp;" + _horario.preencheSigla(_locale) + "&nbsp;");
				_listaVerbeteDiaria.add(2, new String("&nbsp;"));
				_listaVerbeteDiaria.add(3, new String("&nbsp;"));
				_listaVerbeteDiaria.add(4, new String("&nbsp;"));

				_listaVerbeteDiaria.add(5, formataMarcacoes());
				//calcula o saldo diario de trabalho

				// consulta das justificacoes do funcionario numa determinada data
				try {
					consultarJustificacoesPorDia();
				} catch (NotExecuteException nee) {
					throw new NotExecuteException(nee.getMessage());
				}

				try {
					buscarParamJustificacoes();
				} catch (NotExecuteException nee) {
					throw new NotExecuteException(nee.getMessage());
				}

				if ((_horario.getSigla() != null)
					&& (_horario.getSigla().equals(Constants.DSC)
						|| _horario.getSigla().equals(Constants.DS)
						|| _horario.getSigla().equals(Constants.FERIADO))) {
					// formata justificacoes para o jsp
					if (_listaParamJustificacoes.size() > 0) {
						ListIterator iterJustificacoes = _listaJustificacoes.listIterator();
						String justificacoes = new String();
						ParamJustificacao paramJustificacao = null;
						while (iterJustificacoes.hasNext()) {
							iterJustificacoes.next();
							paramJustificacao = (ParamJustificacao) _listaParamJustificacoes.get(iterJustificacoes.previousIndex());

							if (paramJustificacao.getTipoDias().equals(Constants.TODOS)) {
								// formatacao necessaria para o caso de varias justificacoes por dia
								justificacoes = justificacoes.concat(new String(paramJustificacao.getSigla()));
								if (iterJustificacoes.hasNext()) {
									justificacoes = justificacoes.concat("<br>");
								}
							} else {
								justificacoes = justificacoes.concat("&nbsp;");
							}
						}
						_listaVerbeteDiaria.set(4, justificacoes);
					} else { // nao ha justificacoes nos dias de descanso
						if (_listaMarcacoesPonto.size() > 0) {
							calendario.clear();
							calendario.setTimeInMillis(((Long) _listaSaldos.get(0)).longValue());
							_listaVerbeteDiaria.set(2, FormataCalendar.horasMinutosDuracao(calendario));

							IStrategyHorarios horarioStrategy = SuporteStrategyHorarios.getInstance().callStrategy(_horario.getModalidade());
							if (((Long) _listaSaldos.get(0)).longValue() > horarioStrategy.duracaoDiaria(_horario)) {
								_listaSaldos.set(0, new Long(horarioStrategy.duracaoDiaria(_horario)));
							}
							if (_horario.getSigla().equals(Constants.DSC) || _horario.getSigla().equals(Constants.FERIADO)) {
								_saldoDSC = _saldoDSC + ((Long) _listaSaldos.get(0)).longValue();
							} else if (_horario.getSigla().equals(Constants.DS)) {
								_saldoDS = _saldoDS + ((Long) _listaSaldos.get(0)).longValue();
							}
						}
					}
				} else {
					if (_listaParamJustificacoes.size() > 0) {
						// acrescenta as justificacoes na lista de marcacoes de ponto como se fossem marcacoes de ponto
						completaListaMarcacoes();
					}
					// nao efectua calculos para dias al�m do dia de hoje
					if (!_dataConsulta.after(agoraTimestamp)) {
						IStrategyHorarios horarioStrategy = SuporteStrategyHorarios.getInstance().callStrategy(_horario.getModalidade());
						horarioStrategy.setSaldosHorarioVerbeteBody(
							_horario,
							_listaRegimes,
							_listaParamJustificacoes,
							_listaMarcacoesPonto,
							_listaSaldos);
					}
					calendario.clear();
					calendario.setTimeInMillis(((Long) _listaSaldos.get(0)).longValue());
					_listaVerbeteDiaria.set(2, FormataCalendar.horasMinutosDuracao(calendario));
					calendario.clear();
					calendario.setTimeInMillis(((Long) _listaSaldos.get(1)).longValue());
					_listaVerbeteDiaria.set(3, FormataCalendar.horasMinutosDuracao(calendario));

					// formata justificacoes para o jsp e retira tempo justificado do saldo global
					if (_listaParamJustificacoes.size() > 0) {
						ListIterator iterJustificacoes = _listaJustificacoes.listIterator();
						String saldosHN = new String();
						String saldosPF = new String();
						String justificacoes = new String();
						ParamJustificacao paramJustificacao = null;
						Justificacao justificacao = null;
						while (iterJustificacoes.hasNext()) {
							justificacao = (Justificacao) iterJustificacoes.next();
							paramJustificacao = (ParamJustificacao) _listaParamJustificacoes.get(iterJustificacoes.previousIndex());

							IStrategyJustificacoes justificacaoStrategy =
								SuporteStrategyJustificacoes.getInstance().callStrategy(paramJustificacao.getTipo());
							justificacaoStrategy.updateSaldosHorarioVerbeteBody(
								justificacao,
								paramJustificacao,
								_horario,
								_listaRegimes,
								_listaMarcacoesPonto,
								_listaSaldos);

							calendario.clear();
							calendario.setTimeInMillis(((Long) _listaSaldos.get(0)).longValue());
							_listaVerbeteDiaria.set(2, FormataCalendar.horasMinutosDuracao(calendario));

							calendario.clear();
							calendario.setTimeInMillis(((Long) _listaSaldos.get(1)).longValue());
							_listaVerbeteDiaria.set(3, FormataCalendar.horasMinutosDuracao(calendario));

							// formatacao necessaria para o caso de varias justificacoes por dia
							saldosHN = saldosHN.concat((String) _listaVerbeteDiaria.get(2));
							saldosPF = saldosPF.concat((String) _listaVerbeteDiaria.get(3));
							justificacoes = justificacoes.concat(new String(paramJustificacao.getSigla()));
							if (iterJustificacoes.hasNext()) {
								saldosHN = saldosHN.concat("<br>");
								saldosPF = saldosPF.concat("<br>");
								justificacoes = justificacoes.concat("<br>");
							}
						}
						_listaVerbeteDiaria.set(2, saldosHN);
						_listaVerbeteDiaria.set(3, saldosPF);
						_listaVerbeteDiaria.set(4, justificacoes);
					}

					// verifica se este dia nao teve assiduidade
					if (_listaMarcacoesPonto.size() == 0 && _listaParamJustificacoes.size() == 0) {
						_listaVerbeteDiaria.set(4, new String("<b>" + Constants.INJUSTIFICADO + "</b>"));
					}

					// saldo global dos dias de consulta n�o conta com o dia de hoje pois ainda n�o terminou
					if (_dataConsulta.before(agoraTimestamp)) {
						_saldoHNFinal = _saldoHNFinal + ((Long) _listaSaldos.get(0)).longValue();						
						_saldoPFFinal = _saldoPFFinal + ((Long) _listaSaldos.get(1)).longValue();
						// calculo das horas extraordinarias diurnas e nocturnas
						if (((Long) _listaSaldos.get(0)).longValue() != 0) {
							calcularHorasEscalao();
						}
					}
				}
				// aumenta um dia no intervalo de consulta do verbete
				calendario.clear();
				calendario.setTimeInMillis(_dataConsulta.getTime());
				calendario.add(Calendar.DAY_OF_MONTH, +1);
				_dataConsulta.setTime(calendario.getTimeInMillis());

				//junta a lista diaria � lista do verbete
				_listaVerbeteBody.addAll(_listaVerbeteDiaria);
				_listaVerbeteDiaria.clear();
			}

			descontaSaldoNegativo();
		}

		_listaSaldos.set(0, new Long(_saldoHNFinal));
		_listaSaldos.set(1, new Long(_saldoPFFinal));
		_listaSaldos.set(2, new Long(_saldoPrimEscalao));
		_listaSaldos.set(3, new Long(_saldoSegEscalao));
		_listaSaldos.set(4, new Long(_saldoDepoisSegEscalao));
		_listaSaldos.set(5, new Long(_saldoDSC));
		_listaSaldos.set(6, new Long(_saldoDS));
		_listaSaldos.set(7, new Long(_saldoNocturno));
		_listaSaldos.set(8, new Long(_saldoNoctPrimEscalao));
		_listaSaldos.set(9, new Long(_saldoNoctSegEscalao));
		_listaSaldos.set(10, new Long(_saldoNoctDepoisSegEscalao));
	}

	private void lerStatusAssiduidade() throws NotExecuteException {
		ServicoAutorizacao servicoAutorizacao = new ServicoAutorizacao();

		ServicoSeguroLerStatusAssiduidadeFuncionario servicoSeguroLerStatusAssiduidadeFuncionario =
			new ServicoSeguroLerStatusAssiduidadeFuncionario(
				servicoAutorizacao,
				_numMecanografico.intValue(),
				_dataInicioEscolha,
				_dataFimEscolha);
		servicoSeguroLerStatusAssiduidadeFuncionario.execute();

		if (servicoSeguroLerStatusAssiduidadeFuncionario.getListaStatusAssiduidade() != null) {
			if (!servicoSeguroLerStatusAssiduidadeFuncionario.getListaEstadosStatusAssiduidade().contains(Constants.ASSIDUIDADE_ACTIVO)) {
				throw new NotExecuteException("error.assiduidade.naoExiste");
			}
		}
	} /* lerStatusAssiduidade */

	private void lerFimAssiduidade() throws NotExecuteException {
		ServicoAutorizacao servicoAutorizacao = new ServicoAutorizacao();

		ServicoSeguroLerFimAssiduidade servicoSeguroLerFimAssiduidade =
			new ServicoSeguroLerFimAssiduidade(servicoAutorizacao, _numMecanografico.intValue(), _dataInicioEscolha, _dataFimEscolha);
		servicoSeguroLerFimAssiduidade.execute();

		if (servicoSeguroLerFimAssiduidade.getDataAssiduidade() != null) {
			_dataFimEscolha = new Timestamp(servicoSeguroLerFimAssiduidade.getDataAssiduidade().getTime());
		}
	} /* lerFimAssiduidade */

	private void lerInicioAssiduidade() throws NotExecuteException {
		ServicoAutorizacao servicoAutorizacao = new ServicoAutorizacao();

		ServicoSeguroLerInicioAssiduidade servicoSeguroLerInicioAssiduidade =
			new ServicoSeguroLerInicioAssiduidade(servicoAutorizacao, _numMecanografico.intValue(), _dataInicioEscolha, _dataFimEscolha);
		servicoSeguroLerInicioAssiduidade.execute();

		if (servicoSeguroLerInicioAssiduidade.getDataAssiduidade() != null) {
			_dataConsulta = new Timestamp(servicoSeguroLerInicioAssiduidade.getDataAssiduidade().getTime());
			_dataInicioEscolha = servicoSeguroLerInicioAssiduidade.getDataAssiduidade();
		}
	} /* lerInicioAssiduidade */

	private void construirEscolhasMarcacoesPonto() throws NotExecuteException {
		_listaFuncionarios.add(_numMecanografico);

		ServicoAutorizacaoLer servicoAutorizacao = new ServicoAutorizacaoLer();
		ServicoSeguroConstruirEscolhasMarcacoesPonto servicoSeguro =
			new ServicoSeguroConstruirEscolhasMarcacoesPonto(
				servicoAutorizacao,
				_listaFuncionarios,
				_listaCartoes,
				_dataInicioEscolha,
				_dataFimEscolha);
		servicoSeguro.execute();
		_listaCartoes = servicoSeguro.getListaCartoes();
	} /* construirEscolhasMarcacoesPonto */

	private void verificarMesInjustificado() throws NotExecuteException {
		Calendar agora = Calendar.getInstance();
		agora.set(Calendar.HOUR_OF_DAY, 23);
		agora.set(Calendar.MINUTE, 59);
		agora.set(Calendar.SECOND, 59);
		agora.set(Calendar.MILLISECOND, 00);
		Timestamp agoraTimestamp = new Timestamp(agora.getTimeInMillis());

		Calendar calendarioInicio = Calendar.getInstance();
		calendarioInicio.setLenient(false);
		calendarioInicio.setTimeInMillis(_dataInicioEscolha.getTime());

		Calendar calendarioFim = Calendar.getInstance();
		calendarioFim.setLenient(false);
		calendarioFim.setTimeInMillis(_dataFimEscolha.getTime());

		// se estivermos dentro do mesmo mes e o fim de consulta nao for o dia de hoje
		if (calendarioInicio.get(Calendar.MONTH) == calendarioFim.get(Calendar.MONTH) && _dataFimEscolha.before(agoraTimestamp)) {
			Calendar mes = Calendar.getInstance();
			mes.setLenient(false);
			mes.clear();
			mes.set(Calendar.MONTH, calendarioInicio.get(Calendar.MONTH));

			// verificar se a consulta abrange todo o mes 
			if ((calendarioInicio.get(Calendar.DAY_OF_MONTH) == mes.getActualMinimum(Calendar.DAY_OF_MONTH))
				&& (calendarioFim.get(Calendar.DAY_OF_MONTH) == mes.getActualMaximum(Calendar.DAY_OF_MONTH))) {

				if (!verAssiduidade(_dataInicioEscolha, _dataFimEscolha)) {
					// o funcionario faltou o mes todo, logo tem que se avisar no verbete
					_mesInjustificado = true;

					construirListaDiasInjustificados(_dataFimEscolha);
				}
			}
		}
	} /* verificarMesInjustificado */

	private boolean verAssiduidade(Timestamp dataInicio, Timestamp dataFim) throws NotExecuteException {

		Calendar calendarioInicio = Calendar.getInstance();
		calendarioInicio.setLenient(false);
		calendarioInicio.setTimeInMillis(dataInicio.getTime());
		Calendar calendarioFim = Calendar.getInstance();
		calendarioFim.setLenient(false);
		calendarioFim.setTimeInMillis(dataFim.getTime());
		ServicoAutorizacao servicoAutorizacao = new ServicoAutorizacao();

		ServicoSeguroLerMarcacoesPonto servicoSeguroLerMarcacoesPonto =
			new ServicoSeguroLerMarcacoesPonto(
				servicoAutorizacao,
				_listaFuncionarios,
				_listaCartoes,
				null,
				new Timestamp(calendarioInicio.getTimeInMillis()),
				new Timestamp(calendarioFim.getTimeInMillis()));
		servicoSeguroLerMarcacoesPonto.execute();
		ArrayList lista = servicoSeguroLerMarcacoesPonto.getListaMarcacoesPonto();

		if ((lista == null) || (lista.size() <= 0)) {
			ServicoSeguroLerJustificacoesComValidade servicoSeguroLerJustificacoes =
				new ServicoSeguroLerJustificacoesComValidade(
					servicoAutorizacao,
					_numMecanografico.intValue(),
					new Date(calendarioInicio.getTimeInMillis()),
					new Date(calendarioFim.getTimeInMillis()));
			servicoSeguroLerJustificacoes.execute();

			lista = servicoSeguroLerJustificacoes.getListaJustificacoes();
			if ((lista == null) || (lista.size() <= 0)) {
				// o funcionario nao teve assiduidade neste intervalo de datas
				return false;
			}
		}
		return true;
	} /* verAssiduidade */

	private void construirListaDiasInjustificados(Timestamp dataFim) throws NotExecuteException {
		Calendar calendario = Calendar.getInstance();
		calendario.setLenient(false);
		while (_dataConsulta.before((Timestamp) dataFim)) {
			calendario.clear();
			calendario.setTimeInMillis(_dataConsulta.getTime());
			_listaVerbeteDiaria.add(0, FormataCalendar.data(calendario));

			// consulta do horario do funcionario numa determinada data
			try {
				lerHorario();
			} catch (NotExecuteException nee) {
				throw new NotExecuteException(nee.getMessage());
			}

			_listaVerbeteDiaria.add(1, "&nbsp;" + _horario.preencheSigla(_locale) + "&nbsp;");
			_listaVerbeteDiaria.add(2, new String("&nbsp;"));
			_listaVerbeteDiaria.add(3, new String("&nbsp;"));

			_listaJustificacoes = new ArrayList();
			_listaParamJustificacoes = new ArrayList();
			_listaVerbeteDiaria.add(4, new String("<b>" + Constants.INJUSTIFICADO + "</b>"));

			_listaMarcacoesPonto = new ArrayList();
			_listaVerbeteDiaria.add(5, formataMarcacoes());
			//calcula o saldo diario de trabalho

			if (!((_horario.getSigla() != null)
				&& (_horario.getSigla().equals(Constants.DSC)
					|| _horario.getSigla().equals(Constants.DS)
					|| _horario.getSigla().equals(Constants.FERIADO)))) {
				IStrategyHorarios horarioStrategy = SuporteStrategyHorarios.getInstance().callStrategy(_horario.getModalidade());
				horarioStrategy.setSaldosHorarioVerbeteBody(
					_horario,
					_listaRegimes,
					_listaParamJustificacoes,
					_listaMarcacoesPonto,
					_listaSaldos);

				calendario.clear();
				calendario.setTimeInMillis(((Long) _listaSaldos.get(0)).longValue());
				_listaVerbeteDiaria.set(2, FormataCalendar.horasMinutosDuracao(calendario));
				calendario.clear();
				calendario.setTimeInMillis(((Long) _listaSaldos.get(1)).longValue());
				_listaVerbeteDiaria.set(3, FormataCalendar.horasMinutosDuracao(calendario));

				_saldoHNFinal = _saldoHNFinal + ((Long) _listaSaldos.get(0)).longValue();
				_saldoPFFinal = _saldoPFFinal + ((Long) _listaSaldos.get(1)).longValue();
			}
			// diminui um dia no intervalo de consulta do verbete
			calendario.clear();
			calendario.setTimeInMillis(_dataConsulta.getTime());
			calendario.add(Calendar.DAY_OF_MONTH, +1);
			_dataConsulta.setTime(calendario.getTimeInMillis());

			//junta a lista di�ria � lista verbete
			_listaVerbeteBody.addAll(_listaVerbeteDiaria);
			_listaVerbeteDiaria.clear();
		}
	} /* construirListaDiasInjustificados */

	private void lerHorario() throws NotExecuteException {

		ServicoAutorizacao servicoAutorizacao = new ServicoAutorizacao();
		ServicoSeguroLerHorario servicoSeguro =
			new ServicoSeguroLerHorario(servicoAutorizacao, _numMecanografico.intValue(), _dataConsulta);

		servicoSeguro.execute();
		_horario = servicoSeguro.getHorario();
		_listaRegimes = servicoSeguro.getListaRegimes();
	} /* lerHorario */

	private void verificarStatusAssiduidade() throws NotExecuteException {
		Calendar calendarioFim = Calendar.getInstance();
		calendarioFim.setLenient(false);
		calendarioFim.setTimeInMillis(_dataConsulta.getTime());
		/*
		if (_dataConsulta.getTime() == Timestamp.valueOf("2003-01-05 00:00:00.0").getTime()) {
			ArrayList lista = new ArrayList();
			lista.add("pendente");
			while (lista.contains(Constants.STATUS_PENDENTE)) {
				//			enquanto tiver status pendente escreve o status a que corresponde
				construirListaDiasPendente(new StatusAssiduidade(0, "ola", "pendente"), new Timestamp(calendarioFim.getTimeInMillis()));

				calendarioFim.add(Calendar.DAY_OF_MONTH, 1);
				//			se o ciclo ultrapassar a data fim de escolha j� n�o nos interessa continuar
				if (calendarioFim.getTimeInMillis() > _dataFimEscolha.getTime()) {
					return;
				}
				if (_dataConsulta.getTime() == Timestamp.valueOf("2003-01-14 00:00:00.0").getTime()) {
					return;
				}
				_statusPendente = true;
			}
		}*/

		//		verifica se neste dia tem status pendente
		ServicoAutorizacao servicoAutorizacao = new ServicoAutorizacao();
		ServicoSeguroLerStatusAssiduidadeFuncionario servicoSeguroLerStatusAssiduidadeFuncionario =
			new ServicoSeguroLerStatusAssiduidadeFuncionario(servicoAutorizacao, _numMecanografico.intValue(), _dataConsulta, _dataConsulta);
		servicoSeguroLerStatusAssiduidadeFuncionario.execute();

		while (servicoSeguroLerStatusAssiduidadeFuncionario.getListaEstadosStatusAssiduidade().contains(Constants.ASSIDUIDADE_PENDENTE)) {
			//			enquanto tiver status pendente escreve o status a que corresponde
			construirListaDiasPendente(
				(StatusAssiduidade) servicoSeguroLerStatusAssiduidadeFuncionario.getListaStatusAssiduidade().get(0),
				new Timestamp(calendarioFim.getTimeInMillis()));

			calendarioFim.add(Calendar.DAY_OF_MONTH, 1);
			//			se o ciclo ultrapassar a data fim de escolha j� n�o nos interessa continuar
			if (calendarioFim.getTimeInMillis() > _dataFimEscolha.getTime()) {
				return;
			}

			servicoSeguroLerStatusAssiduidadeFuncionario =
				new ServicoSeguroLerStatusAssiduidadeFuncionario(
					servicoAutorizacao,
					_numMecanografico.intValue(),
					_dataConsulta,
					_dataConsulta);
			servicoSeguroLerStatusAssiduidadeFuncionario.execute();
			_statusPendente = true;
		}
	} /* verificarStatusAssiduidade */

	private void construirListaDiasPendente(StatusAssiduidade status, Timestamp dataFim) throws NotExecuteException {
		Calendar calendario = Calendar.getInstance();
		calendario.setLenient(false);
		while (!_dataConsulta.after((Timestamp) dataFim)) {
			calendario.clear();
			calendario.setTimeInMillis(_dataConsulta.getTime());
			_listaVerbeteDiaria.add(0, FormataCalendar.data(calendario));

			// consulta do horario do funcionario numa determinada data
			try {
				lerHorario();
			} catch (NotExecuteException nee) {
				throw new NotExecuteException(nee.getMessage());
			}

			_listaVerbeteDiaria.add(1, "&nbsp;" + _horario.preencheSigla(_locale) + "&nbsp;");
			_listaVerbeteDiaria.add(2, new String("00:00"));
			_listaVerbeteDiaria.add(3, new String("00:00"));

			_listaJustificacoes = new ArrayList();
			_listaParamJustificacoes = new ArrayList();
			_listaVerbeteDiaria.add(4, new String("<b>" + status.getSigla() + "</b>"));

			_listaMarcacoesPonto = new ArrayList();
			_listaVerbeteDiaria.add(5, formataMarcacoes());

			// aumenta um dia no intervalo de consulta do verbete
			calendario.clear();
			calendario.setTimeInMillis(_dataConsulta.getTime());
			calendario.add(Calendar.DAY_OF_MONTH, +1);
			_dataConsulta.setTime(calendario.getTimeInMillis());

			//junta a lista di�ria � lista verbete
			_listaVerbeteBody.addAll(_listaVerbeteDiaria);
			_listaVerbeteDiaria.clear();
		}
	} /* construirListaDiasPendente */

	private void verificarDiasInjustificados(Calendar calendario) throws NotExecuteException {
		IFeriadoPersistente iFeriadoPersistente = SuportePersistente.getInstance().iFeriadoPersistente();

		if (_horario.getSigla() == Constants.DSC || _horario.getSigla() == Constants.FERIADO) {

			// encontrar a data inicio de consulta
			Calendar calendarioAnterior = Calendar.getInstance();
			calendarioAnterior.setLenient(false);
			calendarioAnterior.setTimeInMillis(calendario.getTimeInMillis());
			while (calendarioAnterior.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY
				|| calendarioAnterior.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
				|| iFeriadoPersistente.diaFeriado(calendarioAnterior.getTime())) {
				calendarioAnterior.add(Calendar.DAY_OF_MONTH, -1);
			}

			// encontrar a data fim de consulta
			Calendar calendarioPosterior = Calendar.getInstance();
			calendarioPosterior.setLenient(false);
			calendarioPosterior.setTimeInMillis(calendario.getTimeInMillis());
			calendarioPosterior.set(Calendar.HOUR_OF_DAY, 23);
			calendarioPosterior.set(Calendar.MINUTE, 59);
			calendarioPosterior.set(Calendar.SECOND, 59);
			while (calendarioPosterior.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY
				|| calendarioPosterior.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
				|| iFeriadoPersistente.diaFeriado(calendarioPosterior.getTime())) {
				calendarioPosterior.add(Calendar.DAY_OF_MONTH, 1);
			}
			Timestamp dataFimConsulta = new Timestamp(calendarioPosterior.getTimeInMillis());

			// a data fim de dias injustificados nao pode ser posterior � data fim de consulta
			if (dataFimConsulta.after(_dataFimEscolha)) {
				dataFimConsulta = _dataFimEscolha;
			}
			if (!verAssiduidade(new Timestamp(calendarioAnterior.getTimeInMillis()), dataFimConsulta)) {
				construirListaDiasInjustificados(dataFimConsulta);
				_diasInjustificados = true;
			}
		}
	} /* verificarDiasInjustificados */

	public void calcularIntervaloConsulta() {

		Calendar calendario = Calendar.getInstance();
		calendario.setLenient(false);

		_calendarioConsulta.clear();
		_calendarioConsulta.setTimeInMillis(_dataConsulta.getTime());
		_calendarioConsulta.set(Calendar.HOUR_OF_DAY, 01);
		_calendarioConsulta.set(Calendar.MINUTE, 00);
		_calendarioConsulta.set(Calendar.SECOND, 00);

		calendario.clear();
		calendario.setTimeInMillis(_horario.getInicioExpediente().getTime());
		calendario.add(Calendar.DAY_OF_MONTH, 1);
		long margemExpediente = (calendario.getTimeInMillis() - _horario.getFimExpediente().getTime()) / 2;

		calendario.clear();
		calendario.setTimeInMillis(_calendarioConsulta.getTimeInMillis() + _horario.getInicioExpediente().getTime() - margemExpediente);
		_dataInicio = new Timestamp(calendario.getTimeInMillis());
		calendario.clear();
		calendario.setTimeInMillis(
			_calendarioConsulta.getTimeInMillis() + _horario.getFimExpediente().getTime() + margemExpediente - 60 * 1000);
		_dataFim = new Timestamp(calendario.getTimeInMillis());
	} /* calcularIntervaloConsulta */

	private void consultarMarcacoesPonto() throws NotExecuteException {
		_listaEstados.add(new String("valida"));
		_listaEstados.add(new String("regularizada"));
		_listaEstados.add(new String("cartaoFuncionarioInvalido"));
		_listaEstados.add(new String("cartaoSubstitutoInvalido"));

		ServicoAutorizacao servicoAutorizacao = new ServicoAutorizacao();
		ServicoSeguroConsultarMarcacaoPonto servicoSeguro =
			new ServicoSeguroConsultarMarcacaoPonto(
				servicoAutorizacao,
				_listaFuncionarios,
				_listaCartoes,
				_listaEstados,
				_dataInicio,
				_dataFim);
		servicoSeguro.execute();
		_listaMarcacoesPonto = servicoSeguro.getListaMarcacoesPonto();
	} /* consultarMarcacaoPonto */

	private String formataMarcacoes() {
		Calendar calendario = Calendar.getInstance();
		long saldo = 0;
		String marcacoes = new String();

		if (_listaMarcacoesPonto.size() > 0) {
			// lista ordenada das marcacoes de ponto
			Comparador comparadorMarcacoes = new Comparador(new String("MarcacaoPonto"), new String("crescente"));
			Collections.sort(_listaMarcacoesPonto, comparadorMarcacoes);

			// formatacao do campo das marcacoes
			ListIterator iteradorMarcacoes = _listaMarcacoesPonto.listIterator();
			MarcacaoPonto entrada = null;
			MarcacaoPonto saida = null;

			while (iteradorMarcacoes.hasNext()) {
				entrada = (MarcacaoPonto) iteradorMarcacoes.next();

				//escreve marcacao no verbete
				calendario.clear();
				calendario.setTime(entrada.getData());
				if (entrada.getEstado().equals("regularizada")) {
					marcacoes = marcacoes.concat("<b>" + FormataCalendar.horas(calendario) + "</b>");
				} else {
					marcacoes = marcacoes.concat(FormataCalendar.horas(calendario));
				}
				// para o calculo do saldo
				calendario.setTimeInMillis(entrada.getData().getTime() - _calendarioConsulta.getTimeInMillis());
				calendario.set(Calendar.SECOND, 00);
				entrada.setData(new Timestamp(calendario.getTimeInMillis()));

				if (iteradorMarcacoes.hasNext()) {
					saida = (MarcacaoPonto) iteradorMarcacoes.next();
					//escreve marcacao no verbete
					calendario.clear();
					calendario.setTime(saida.getData());
					marcacoes = marcacoes.concat("-");
					if (saida.getEstado().equals("regularizada")) {
						marcacoes = marcacoes.concat("<b>" + FormataCalendar.horas(calendario) + "</b>");
					} else {
						marcacoes = marcacoes.concat(FormataCalendar.horas(calendario));
					}
					// para o calculo do saldo
					calendario.setTimeInMillis(saida.getData().getTime() - _calendarioConsulta.getTimeInMillis());
					calendario.set(Calendar.SECOND, 00);
					saida.setData(new Timestamp(calendario.getTimeInMillis()));

					if (entrada.getData().getTime() < _horario.getInicioExpediente().getTime()) {
						entrada.setData(new Timestamp(_horario.getInicioExpediente().getTime()));
					}
					if (saida.getData().getTime() > _horario.getFimExpediente().getTime()) {
						saida.setData(new Timestamp(_horario.getFimExpediente().getTime()));
					}
					saldo = saldo + saida.getData().getTime() - entrada.getData().getTime();
					if ((_horario.getSigla() != null)
						&& !((_horario.getSigla().equals(Constants.DSC)
							|| _horario.getSigla().equals(Constants.DS)
							|| _horario.getSigla().equals(Constants.FERIADO)))) {
						calcularTrabalhoNocturno(entrada, saida);
					}
				}
			}
		} else { // nao existem marcacoes de ponto neste dia
			marcacoes = marcacoes.concat("&nbsp;");
		}
		_listaSaldos.clear();
		_listaSaldos.add(new Long(saldo)); // horario normal
		_listaSaldos.add(new Long(0)); // plataformas fixas
		_listaSaldos.add(new Long(0));
		// trabalho extraordin�rio 1� escal�o
		_listaSaldos.add(new Long(0));
		// trabalho extraordin�rio 2� escal�o
		_listaSaldos.add(new Long(0));
		// trabalho extraordin�rio depois do 2� escal�o
		_listaSaldos.add(new Long(0));
		// horas em dia de descanso complementar DSC
		_listaSaldos.add(new Long(0));
		// horas em dia de descanso semanal DS
		_listaSaldos.add(new Long(0)); // trabalho nocturno
		_listaSaldos.add(new Long(0));
		// trabalho extraordin�rio nocturno 1� escalao
		_listaSaldos.add(new Long(0));
		// trabalho extraordin�rio nocturno 2� escalao
		_listaSaldos.add(new Long(0));
		// trabalho extraordin�rio nocturno depois 2� escalao

		return marcacoes;
	} /* formataMarcacoes */

	private void consultarJustificacoesPorDia() throws NotExecuteException {

		ServicoAutorizacao servicoAutorizacao = new ServicoAutorizacao();
		ServicoSeguroConsultarJustificacoesPorDia servicoSeguro =
			new ServicoSeguroConsultarJustificacoesPorDia(servicoAutorizacao, _numMecanografico.intValue(), _dataInicio);
		servicoSeguro.execute();
		_listaJustificacoes = servicoSeguro.getListaJustificacoes();

		// lista ordenada das justificacoes
		Comparador comparadorJustificacoes = new Comparador(new String("Justificacao"), new String("crescente"));
		Collections.sort(_listaJustificacoes, comparadorJustificacoes);
	} /* consultarJustificacoesPorDia */

	private void buscarParamJustificacoes() throws NotExecuteException {

		ServicoAutorizacao servicoAutorizacao = new ServicoAutorizacao();
		ServicoSeguroBuscarParamJustificacoes servicoSeguro =
			new ServicoSeguroBuscarParamJustificacoes(servicoAutorizacao, _listaJustificacoes);

		servicoSeguro.execute();
		_listaParamJustificacoes = servicoSeguro.getListaJustificacoes();
	} /* buscarParamJustificacoes */

	private void completaListaMarcacoes() {
		ListIterator iterador = _listaJustificacoes.listIterator();
		while (iterador.hasNext()) {
			Justificacao justificacao = (Justificacao) iterador.next();
			ParamJustificacao paramJustificacao = (ParamJustificacao) _listaParamJustificacoes.get(iterador.previousIndex());

			IStrategyJustificacoes justificacaoStrategy =
				SuporteStrategyJustificacoes.getInstance().callStrategy(paramJustificacao.getTipo());
			justificacaoStrategy.completaListaMarcacoes(justificacao, _listaMarcacoesPonto);
		}
		Comparador comparadorMarcacoes = new Comparador(new String("MarcacaoPonto"), new String("crescente"));
		Collections.sort(_listaMarcacoesPonto, comparadorMarcacoes);
	} /* completaListaMarcacoes */

	private void calcularHorasEscalao() {
		long saldo = ((Long) _listaSaldos.get(0)).longValue();

		if (saldo > 0) {
			IStrategyHorarios horarioStrategy = SuporteStrategyHorarios.getInstance().callStrategy(_horario.getModalidade());
			horarioStrategy.calcularHorasExtraordinarias(_horario, _listaMarcacoesPonto, _listaSaldos);

			_saldoPrimEscalao = _saldoPrimEscalao + ((Long) _listaSaldos.get(2)).longValue();
			_saldoSegEscalao = _saldoSegEscalao + ((Long) _listaSaldos.get(3)).longValue();
			_saldoDepoisSegEscalao = _saldoDepoisSegEscalao + ((Long) _listaSaldos.get(4)).longValue();
			_saldoNoctPrimEscalao = _saldoNoctPrimEscalao + ((Long) _listaSaldos.get(8)).longValue();
			_saldoNoctSegEscalao = _saldoNoctSegEscalao + ((Long) _listaSaldos.get(9)).longValue();
			_saldoNoctDepoisSegEscalao = _saldoNoctDepoisSegEscalao + ((Long) _listaSaldos.get(10)).longValue();
		} else {
			_saldoNegativo = _saldoNegativo + saldo;
		}
	} /* calcularHorasEscalao */

	private void descontaSaldoNegativo() {
		// valor negativo de saldo, portanto retira o valor em primeiro lugar do saldoDepoisSegEscalao,
		// depois do saldoSegEscalao e por ultimo do saldoPrimEscalao
		if (_saldoPrimEscalao != 0 || _saldoSegEscalao != 0 || _saldoDepoisSegEscalao != 0) {
			if (-_saldoNegativo > _saldoDepoisSegEscalao) {
				_saldoNegativo = _saldoNegativo + _saldoDepoisSegEscalao;
				_saldoDepoisSegEscalao = 0;
				if (-_saldoNegativo > _saldoSegEscalao) {
					_saldoNegativo = _saldoNegativo + _saldoSegEscalao;
					_saldoSegEscalao = 0;
					if (-_saldoNegativo > _saldoPrimEscalao) {
						_saldoPrimEscalao = 0;
					} else {
						_saldoPrimEscalao = _saldoPrimEscalao + _saldoNegativo;
					}
				} else {
					_saldoSegEscalao = _saldoSegEscalao + _saldoNegativo;
				}
			} else {
				_saldoDepoisSegEscalao = _saldoDepoisSegEscalao + _saldoNegativo;
			}
		}
	} /* descontaSaldoNegativo */

	private void calcularTrabalhoNocturno(MarcacaoPonto entrada, MarcacaoPonto saida) {
		IStrategyHorarios horarioStrategy = SuporteStrategyHorarios.getInstance().callStrategy(_horario.getModalidade());

		_saldoNocturno = _saldoNocturno + horarioStrategy.calcularTrabalhoNocturno(_horario, entrada, saida);
	} /* calcularTrabalhoNocturno */

	public ArrayList getVerbete() {
		return _listaVerbeteBody;
	}
	public ArrayList getListaSaldos() {
		return _listaSaldos;
	}
}
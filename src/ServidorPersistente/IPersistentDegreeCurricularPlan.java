package ServidorPersistente;

import java.util.ArrayList;
import java.util.List;

import Dominio.ICurso;
import Dominio.IDegreeCurricularPlan;
import ServidorPersistente.exceptions.ExistingPersistentException;
import Util.DegreeCurricularPlanState;

public interface IPersistentDegreeCurricularPlan extends IPersistentObject {

	public ArrayList readAll() throws ExcepcaoPersistencia;
	public void write(IDegreeCurricularPlan planoCurricular) throws ExcepcaoPersistencia, ExistingPersistentException;
	public Boolean deleteDegreeCurricularPlan(IDegreeCurricularPlan planoCurricular) throws ExcepcaoPersistencia;
	public void deleteAll() throws ExcepcaoPersistencia;
	public IDegreeCurricularPlan readByNameAndDegree(String name, ICurso degree) throws ExcepcaoPersistencia;
	public List readByDegree(ICurso degree) throws ExcepcaoPersistencia;
	public List readByDegreeAndState(ICurso degree,DegreeCurricularPlanState state) throws ExcepcaoPersistencia;
}
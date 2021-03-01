/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.visao;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.table.AbstractTableModel;

/**
 *
 * @author ThyagoHenrique
 */
public class TableModel extends AbstractTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/* Lista de Sócios que representam as linhas. */
	private List<Objeto> linhas = new ArrayList<>(100);

	/* Array de Strings com o nome das colunas. */
	private String[] colunas = new String[] { "Package" + "." + "Class", "Refactoring Method", "Pattern", "Apply" };

	public TableModel() {
		linhas = new ArrayList<>(100);
	}

	public TableModel(List<Objeto> linhas2) {
		linhas = new ArrayList<>(linhas2);
	}

	/* Retorna a quantidade de colunas. */
	@Override
	public int getColumnCount() {
		return colunas.length;
	}

	/* Retorna a quantidade de linhas. */
	@Override
	public int getRowCount() {
		return linhas.size();
	}

	/*
	 * Retorna o nome da coluna no índice especificado. Este método é usado pela
	 * JTable para saber o texto do cabeçalho.
	 */
	@Override
	public String getColumnName(int columnIndex) {
		return colunas[columnIndex];
	};

	/*
	 * Retorna a classe dos elementos da coluna especificada. Este método é usado
	 * pela JTable na hora de definir o editor da célula.
	 */
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
		case 0:
			return String.class;
		case 1:
			return String.class;			
		case 2:
			return String.class;
		case 3:
			return Boolean.class;
		default:
			throw new IndexOutOfBoundsException("columnIndex out of bounds");
		}
	}

	/*
	 * Retorna o valor da célula especificada pelos índices da linha e da coluna.
	 */
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		// Pega o sócio da linha especificada.
		Objeto objeto = linhas.get(rowIndex);

		switch (columnIndex) {
		case 0:
			return objeto.getClasse();
		case 1:
			return objeto.getMetodo();			
		case 2:{
			return objeto.getPadrao().getNome();
		}
		case 3:
			return objeto.isAplicado();
		default:
			throw new IndexOutOfBoundsException("columnIndex out of bounds");
		}
	}

	/*
	 * Seta o valor da célula especificada pelos índices da linha e da coluna. Aqui
	 * ele está implementado para não fazer nada, até porque este table model não é
	 * editável.
	 */
	@Override
	public void setValueAt(Object aValue, int rowIndex, int col) {
		linhas.get(rowIndex).setAplicado((boolean) aValue);
		fireTableRowsUpdated(rowIndex, rowIndex);
	}

	/*
	 * Retorna um valor booleano que define se a célula em questão pode ser editada
	 * ou não. Este método é utilizado pela JTable na hora de definir o editor da
	 * célula. Neste caso, estará sempre retornando false, não permitindo que
	 * nenhuma célula seja editada.
	 */
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return columnIndex == 3;
	}

	public Objeto getObjeto(int indiceLinha) {
		return linhas.get(indiceLinha);
	}

	/* Adiciona um registro. */
	public void addObjeto(Objeto Objeto) {
		linhas.add(Objeto);
		int ultimoIndice = getRowCount() - 1;
		fireTableRowsInserted(ultimoIndice, ultimoIndice);
	}

	/* Remove a linha especificada. */
	public void removeObjeto(int indiceLinha) {
		linhas.remove(indiceLinha);
		fireTableRowsDeleted(indiceLinha, indiceLinha);
	}

	/* Adiciona uma lista de sócios ao final dos registros. */
	public void addListaDeObjetos(List<Objeto> Objetos) {
		int tamanhoAntigo = getRowCount();
		linhas.addAll(Objetos);
		fireTableRowsInserted(tamanhoAntigo, getRowCount() - 1);
	}

	/* Remove todos os registros. */
	public void limpar() {
		linhas.clear();
		fireTableDataChanged();
	}

	/* Verifica se este table model está vazio. */
	public boolean isEmpty() {
		return linhas.isEmpty();
	}
}

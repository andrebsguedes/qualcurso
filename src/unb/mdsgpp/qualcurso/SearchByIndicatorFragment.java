package unb.mdsgpp.qualcurso;

import helpers.Indicator;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;

import models.Article;
import models.Book;
import models.Course;
import models.Evaluation;
import models.Institution;
import models.Search;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class SearchByIndicatorFragment extends Fragment {
	
	BeanListCallbacks beanCallbacks;
	
	public SearchByIndicatorFragment() {
		super();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
            beanCallbacks = (BeanListCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()+" must implement BeanListCallbacks.");
        }
	}

	@Override
    public void onDetach() {
        super.onDetach();
        beanCallbacks = null;
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.search_fragment, container,
				false);
		
		final Spinner listSelectionSpinner = (Spinner) rootView
				.findViewById(R.id.course_institution);
		
		final Spinner filterFieldSpinner = (Spinner) rootView.findViewById(R.id.field);
		filterFieldSpinner.setAdapter(new ArrayAdapter<Indicator>(getActivity().getApplicationContext(), R.layout.simple_textview,Indicator.getIndicators()));
		final Spinner yearSpinner = (Spinner) rootView.findViewById(R.id.year);
		final CheckBox maximum = (CheckBox) rootView.findViewById(R.id.maximum);
		final EditText firstNumber = (EditText) rootView.findViewById(R.id.firstNumber);
		final EditText secondNumber = (EditText) rootView.findViewById(R.id.secondNumber);
		Button searchButton = (Button) rootView.findViewById(R.id.buttonSearch);

		OnClickListener listener = new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				int number1, number2, year, max, listSelectionPosition;

				if( firstNumber.getText().length() == 0) {
					firstNumber.setText("0");
				}

				if( secondNumber.getText().length() == 0 ) {
					maximum.setChecked(true);
				}

				String firstNumberValue = firstNumber.getText().toString();
				String secondNumberValue = secondNumber.getText().toString();

				number1 = Integer.parseInt(firstNumberValue);
				number2 = maximum.isChecked() ? -1 : Integer.parseInt(secondNumberValue);
				listSelectionPosition = listSelectionSpinner.getSelectedItemPosition();

				if( yearSpinner.getSelectedItemPosition() != 0 ) {
					year = Integer.parseInt(yearSpinner.getSelectedItem().toString());
				} else {
					year = Integer.parseInt(yearSpinner.getAdapter().getItem(yearSpinner.getAdapter().getCount()-1).toString());
				}

				if(maximum.isChecked()){
					max = -1;
				}else{
					max = number2;
				}
					this.updateSearchList(number1, max, year, listSelectionPosition, ((Indicator)filterFieldSpinner.getItemAtPosition(filterFieldSpinner.getSelectedItemPosition())));
			
			}

			private void updateSearchList(int min, int max, int year, int listSelectionPosition, Indicator filterField) {
				if(filterField.getValue() == Indicator.DEFAULT_INDICATOR) {
					Context c = QualCurso.getInstance();
					String emptySearchFilter = getResources().getString(R.string.empty_search_filter);

					Toast toast = Toast.makeText(c, emptySearchFilter, Toast.LENGTH_SHORT);
					toast.show();
				} else {
						switch (listSelectionPosition) {
						case 0:
							listSelectionSpinner.setSelection(listSelectionSpinner.getAdapter().getCount()-1);
							yearSpinner.setSelection(yearSpinner.getAdapter().getCount()-1);

							callInstitutionList(min, max, year, filterField);
							break;

						case 1:
							callCourseList(min, max, year, filterField);
							break;

						case 2:
							callInstitutionList(min, max, year, filterField);
							break;

						default:
							break;
						}
				}
			}

			private void callInstitutionList(int min, int max, int year, Indicator filterField){
				Calendar c = Calendar.getInstance();
				Search search = new Search();
				search.setDate(new Date(c.getTime().getTime()));
				search.setYear(year);
				search.setOption(1);
				search.setIndicator(filterField);
				search.setMinValue(min);
				search.setMaxValue(max);
				search.save();
				ArrayList<Institution> beanList = Institution.getInstitutionsByEvaluationFilter(search);
				beanCallbacks.onBeanListItemSelected(SearchListFragment.newInstance(beanList,search), R.id.search_list);
			}

			private void callCourseList(int min, int max, int year, Indicator filterField){
				Calendar c = Calendar.getInstance();
				Search search = new Search();
				search.setDate(new Date(c.getTime().getTime()));
				search.setYear(year);
				search.setOption(0);
				search.setIndicator(filterField);
				search.setMinValue(min);
				search.setMaxValue(max);
				search.save();
				ArrayList<Course> beanList = Course.getCoursesByEvaluationFilter(search);
				beanCallbacks.onBeanListItemSelected(SearchListFragment.newInstance(beanList,search), R.id.search_list);
			}
		};

		searchButton.setOnClickListener(listener);

		// Event to disable second number when MAX is checked
		maximum.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if( maximum.isChecked() ) {
					secondNumber.setEnabled(false);
				} else {
					secondNumber.setEnabled(true);
				}
			}
		});

		return rootView;
	}

}
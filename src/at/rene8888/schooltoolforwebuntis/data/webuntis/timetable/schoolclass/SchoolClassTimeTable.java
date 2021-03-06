package at.rene8888.schooltoolforwebuntis.data.webuntis.timetable.schoolclass;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.util.Log;
import at.rene8888.schooltoolforwebuntis.data.ApplicationClass;
import at.rene8888.schooltoolforwebuntis.data.util.time.Time;
import at.rene8888.schooltoolforwebuntis.data.webuntis.Data;
import at.rene8888.schooltoolforwebuntis.data.webuntis.room.Room;
import at.rene8888.schooltoolforwebuntis.data.webuntis.schoolclasses.SchoolClass;
import at.rene8888.schooltoolforwebuntis.data.webuntis.subject.Subject;
import at.rene8888.schooltoolforwebuntis.data.webuntis.teacher.Teacher;
import at.rene8888.schooltoolforwebuntis.data.webuntis.timegrid.TimeGrid;
import at.rene8888.schooltoolforwebuntis.data.webuntis.timegrid.Unit;
import at.rene8888.schooltoolforwebuntis.data.webuntis.timegrid.UnitType;
import at.rene8888.schooltoolforwebuntis.data.webuntis.timetable.TimeTableEntry;
import at.rene8888.schooltoolforwebuntis.data.webuntis.util.SchoolClassTimeTableSorter;

public class SchoolClassTimeTable {

	public StringBuilder sb = new StringBuilder();

	private ArrayList<TimeTableEntry<SchoolClassTimeTableUnit>> units;

	private TimeTableEntry<SchoolClassTimeTableUnit> firstLesson;
	private TimeTableEntry<SchoolClassTimeTableUnit> lastLesson;

	private SchoolClass schoolClass;
	private Calendar cal;

	public SchoolClassTimeTable(SchoolClass schoolClass, Calendar cal) {
		this.schoolClass = schoolClass;
		this.cal = cal;
	}

	private void fillList() {
		try {
			this.units = new ArrayList<TimeTableEntry<SchoolClassTimeTableUnit>>();

			Data data = Data.getData();

			JSONObject params = new JSONObject();
			params.put("id", this.schoolClass.getId());
			params.put("type", "1");
			String date = Time.convertToYYYYMMDD(this.cal);
			params.put("startDate", date);
			params.put("endDate", date);

			JSONArray ja = (JSONArray) ApplicationClass.getApplication().getWebUntisRequests().getData("getTimetable", params);

			for (int i = 0; i < ja.length(); i++) {
				JSONObject curr = ja.getJSONObject(i);

				Time start = Time.createTime(curr.getString("startTime"));
				Time end = Time.createTime(curr.getString("endTime"));

				TimeGrid tg = data.getTimeGrids().getTimeGridByCalendar(this.cal);
				if (tg == null) {
					Log.e("sctt error", "timegrid is null");
					continue;
				}

				Unit unit = tg.getUnitByTime(start, end);

				ArrayList<Room> rooms = new ArrayList<Room>();
				JSONArray roomarr = curr.getJSONArray("ro");
				if (roomarr.length() == 1) {
					rooms.add(data.getRooms().getRoomById(roomarr.getJSONObject(0).getInt("id")));
				} else {
					for (int j = 0; j < roomarr.length(); j++) {
						JSONObject currroom = roomarr.getJSONObject(j);
						rooms.add(data.getRooms().getRoomById(currroom.getInt("id")));
					}
				}

				ArrayList<Subject> subjects = new ArrayList<Subject>();
				JSONArray subjectarr = curr.getJSONArray("su");
				if (subjectarr.length() == 1) {
					subjects.add(data.getSubjects().getSubjectById(subjectarr.getJSONObject(0).getInt("id")));
				} else {
					for (int j = 0; j < subjectarr.length(); j++) {
						JSONObject currsubject = subjectarr.getJSONObject(j);
						subjects.add(data.getSubjects().getSubjectById(currsubject.getInt("id")));
					}
				}

				ArrayList<Teacher> teachers = new ArrayList<Teacher>();
				JSONArray teacherarr = curr.getJSONArray("te");
				if (teacherarr.length() == 1) {
					teachers.add(data.getTeachers().getTeacherById(teacherarr.getJSONObject(0).getInt("id")));
				} else {
					for (int j = 0; j < teacherarr.length(); j++) {
						JSONObject currteacher = teacherarr.getJSONObject(j);
						teachers.add(data.getTeachers().getTeacherById(currteacher.getInt("id")));
					}
				}

				ArrayList<SchoolClass> schoolclasses = new ArrayList<SchoolClass>();
				JSONArray schoolclassarr = curr.getJSONArray("kl");
				if (schoolclassarr.length() == 1) {
					schoolclasses.add(data.getSchoolClasses().getSchoolClassById(schoolclassarr.getJSONObject(0).getInt("id")));
				} else {
					for (int j = 0; j < schoolclassarr.length(); j++) {
						JSONObject currschoolclass = schoolclassarr.getJSONObject(j);
						schoolclasses.add(data.getSchoolClasses().getSchoolClassById(currschoolclass.getInt("id")));
					}
				}

				TimeTableEntry<SchoolClassTimeTableUnit> tte = new TimeTableEntry<SchoolClassTimeTableUnit>(unit, new SchoolClassTimeTableUnit(unit, curr.getInt("id"), rooms, subjects, teachers, schoolclasses));
				this.units.add(tte);

			}

			Collections.sort(this.units, new SchoolClassTimeTableSorter());

			this.firstLesson = this.units.get(0);
			this.lastLesson = this.units.get(this.units.size() - 1);

			for (Unit u : data.getTimeGrids().getTimeGridByCalendar(this.cal).getUnitList()) {
				if (u.after(this.firstLesson.getUnit()) && u.before(this.lastLesson.getUnit())) {
					if (u.getTag() == UnitType.BREAK) {
						this.units.add(new TimeTableEntry<SchoolClassTimeTableUnit>(u, null));
					} else if (containsUnit(u, this.units) == false) {
						Unit newUnit = (Unit) u.clone();
						newUnit.setTag(UnitType.FREEHOUR);
						this.units.add(new TimeTableEntry<SchoolClassTimeTableUnit>(newUnit, null));
					}
				}
			}

			Collections.sort(this.units, new SchoolClassTimeTableSorter());

			this.firstLesson = this.units.get(0);
			this.lastLesson = this.units.get(this.units.size() - 1);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Calendar getCalendar() {
		return this.cal;
	}

	public ArrayList<TimeTableEntry<SchoolClassTimeTableUnit>> getUnits() {
		if (this.units == null) {
			this.fillList();
		}
		return this.units;
	}

	public TimeTableEntry<SchoolClassTimeTableUnit> getFirstLesson() {
		if (this.firstLesson == null) {
			this.fillList();
		}
		return this.firstLesson;
	}

	public TimeTableEntry<SchoolClassTimeTableUnit> getLastLesson() {
		if (this.lastLesson == null) {
			this.fillList();
		}
		return this.lastLesson;
	}

	public TimeTableEntry<SchoolClassTimeTableUnit> getUnitbyUnit(Unit another) {
		for (int i = 0; i < this.getUnits().size(); i++) {
			TimeTableEntry<SchoolClassTimeTableUnit> entry = this.units.get(i);
			if (entry.getUnit().equalByTime(another)) {
				entry.setIndex(i);
				return entry;
			}
		}
		return null;
	}

	public TimeTableEntry<SchoolClassTimeTableUnit> getUnitByIndex(int index) {
		if (index < this.getUnits().size()) {
			TimeTableEntry<SchoolClassTimeTableUnit> entry = this.getUnits().get(index);
			entry.setIndex(index);
			return entry;
		} else {
			return null;
		}
	}

	private static boolean containsUnit(Unit other, List<TimeTableEntry<SchoolClassTimeTableUnit>> list) {
		for (TimeTableEntry<SchoolClassTimeTableUnit> entry : list) {
			if (entry.getUnit().equalByTime(other)) {
				return true;
			}
		}
		return false;
	}

}

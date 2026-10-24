/**
 * All UI strings in English and Telugu.
 * Usage: const { t } = useLang(); then t('en key', 'te key')
 * or import T and use T.key.en / T.key.te
 */

export const T = {
  // Nav
  dueList:           { en: 'Due list',       te: 'విజిట్ జాబితా' },
  patients:          { en: 'Patients',       te: 'రోగులు' },
  referrals:         { en: 'Referrals',      te: 'రిఫెరళ్ళు' },
  dashboard:         { en: 'Dashboard',      te: 'డాష్‌బోర్డ్' },
  rules:             { en: 'Rules',          te: 'నియమాలు' },
  signOut:           { en: 'Sign out',       te: 'నిష్క్రమించు' },

  // DueListPage
  registerPatient:   { en: 'Register patient',            te: 'రోగిని నమోదు చేయండి' },
  yourVisits:        { en: 'Your visits, most urgent first', te: 'మీ విజిట్‌లు, అత్యవసర మొదట' },
  overdue:           { en: 'Overdue',        te: 'గడువు దాటిన' },
  dueToday:          { en: 'Due today',      te: 'ఈ రోజు విజిట్' },
  upcoming:          { en: 'Upcoming',       te: 'రాబోయే' },
  allCaughtUp:       { en: 'All caught up',  te: 'అన్నీ పూర్తయ్యాయి' },
  allCaughtUpDesc:   { en: 'No pending visits. New tasks appear here automatically when a visit or referral needs follow-up.',
                       te: 'పెండింగ్ విజిట్‌లు లేవు. విజిట్ లేదా రిఫెరల్ అవసరమైనప్పుడు కొత్త పనులు ఇక్కడ స్వయంచాలకంగా కనిపిస్తాయి.' },
  missedReferral:    { en: 'Missed referral', te: 'రిఫెరల్ వదిలిపోయింది' },
  newBadge:          { en: 'New',            te: 'కొత్త' },
  due:               { en: 'Due',            te: 'గడువు' },
  recordVisit:       { en: 'Record visit',   te: 'విజిట్ నమోదు చేయండి' },
  markDone:          { en: 'Mark done',      te: 'పూర్తయింది గుర్తించు' },

  // PatientsPage
  patientsTitle:         { en: 'Patients',             te: 'రోగులు' },
  patientsSubtitleAsha:  { en: 'Patients you follow',  te: 'మీరు అనుసరించే రోగులు' },
  patientsSubtitlePhc:   { en: 'Patients in villages your PHC serves', te: 'మీ PHC సేవ చేసే గ్రామాల రోగులు' },
  patientsSubtitleSuper: { en: 'All patients in the block', te: 'బ్లాక్‌లోని అన్ని రోగులు' },
  searchByName:          { en: 'Search by name',       te: 'పేరు వెతకండి' },
  allProgrammes:         { en: 'All programmes',       te: 'అన్ని కార్యక్రమాలు' },
  noPatients:            { en: 'No patients found',    te: 'రోగులు కనుగొనబడలేదు' },
  noPatientsHint:        { en: 'Try a different name or programme.', te: 'వేరే పేరు లేదా కార్యక్రమం ప్రయత్నించండి.' },
  programme:             { en: 'Programme',            te: 'కార్యక్రమం' },

  // RecordVisitPage
  recordVisitTitle:  { en: 'Record visit',     te: 'విజిట్ నమోదు చేయండి' },
  observations:      { en: 'Observations',     te: 'పరిశీలనలు' },
  obsSubtitle:       { en: 'Leave a field empty if you didn\'t measure it.', te: 'కొలవకపోతే ఖాళీగా వదలండి.' },
  bloodPressure:     { en: 'Blood pressure (mmHg)', te: 'రక్తపోటు (mmHg)' },
  bpHint:            { en: 'Systolic / diastolic',   te: 'సిస్టోలిక్ / డయాస్టోలిక్' },
  pregnancyWeeks:    { en: 'Pregnancy (weeks)', te: 'గర్భావస్థ (వారాలు)' },
  pregnancyHint:     { en: 'Estimated from the last visit — correct if needed', te: 'చివరి విజిట్ నుండి అంచనా — అవసరమైతే సరి చేయండి' },
  temperature:       { en: 'Temperature (°C)', te: 'శరీర ఉష్ణోగ్రత (°C)' },
  weight:            { en: 'Weight (kg)',       te: 'బరువు (కిలోలు)' },
  tbDose:            { en: "Today's TB dose taken?", te: 'ఈ రోజు TB మోతాదు తీసుకున్నారా?' },
  yes:               { en: 'Yes, taken',        te: 'అవును, తీసుకున్నారు' },
  no:                { en: 'No, missed',        te: 'లేదు, వదిలిపెట్టారు' },
  symptomsTitle:     { en: 'Symptoms reported', te: 'నివేదించిన లక్షణాలు' },
  symptomsSubtitle:  { en: 'Tick everything the patient reports today.', te: 'రోగి నేడు నివేదించిన అన్నింటినీ టిక్ చేయండి.' },
  notes:             { en: 'Notes (optional)',  te: 'గమనికలు (ఐచ్ఛికం)' },
  cancel:            { en: 'Cancel',            te: 'రద్దు చేయండి' },
  saveVisit:         { en: 'Save visit',        te: 'విజిట్ సేవ్ చేయండి' },
  loadingSymptoms:   { en: 'Loading symptoms…', te: 'లక్షణాలు లోడ్ అవుతున్నాయి…' },

  // Visit outcome
  visitSaved:        { en: 'Visit saved',               te: 'విజిట్ సేవ్ అయింది' },
  referralSent:      { en: 'Referral sent to the PHC',  te: 'PHC కి రిఫెరల్ పంపబడింది' },
  nextVisit:         { en: 'Next visit scheduled',      te: 'తదుపరి విజిట్ షెడ్యూల్ అయింది' },
  noFlags:           { en: 'No flags raised',           te: 'ఏ హెచ్చరికలూ లేవు' },
  whyTitle:          { en: 'Why',                       te: 'ఎందుకు' },
  whySubtitle:       { en: 'Each rule that fired, in plain language', te: 'మండిన ప్రతి నియమం, సాధారణ భాషలో' },
  viewPatient:       { en: 'View patient',              te: 'రోగిని వీక్షించండి' },
  backToDueList:     { en: 'Back to due list',          te: 'విజిట్ జాబితాకు వెనుకకు' },

  // Referrals
  referralsTitle:    { en: 'Referrals',         te: 'రిఫెరళ్ళు' },
  noReferrals:       { en: 'No referrals',      te: 'రిఫెరళ్ళు లేవు' },
  status:            { en: 'Status',            te: 'స్థితి' },
  allStatuses:       { en: 'All statuses',      te: 'అన్ని స్థితులు' },

  // Common
  rule:              { en: 'Rule',        te: 'నియమం' },
  namaskaram:        { en: 'Namaskaram',  te: 'నమస్కారం' },
  loading:           { en: 'Loading…',   te: 'లోడ్ అవుతున్నది…' },
  retry:             { en: 'Retry',      te: 'మళ్ళీ ప్రయత్నించండి' },
  switchToTelugu:    { en: 'Switch to Telugu', te: 'ఇంగ్లీషుకు మారండి' },
} as const

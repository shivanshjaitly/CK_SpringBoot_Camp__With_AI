# Repository Strategy — One Repo vs Many Repos

> **Recommendation: ONE repo for the entire bootcamp.**  
> Repo name: `codekerdos-springboot-ai-bootcamp`

---

## ✅ Recommended: Single repo (what we are doing)

```
codekerdos-springboot-ai-bootcamp/
├── README.md
├── docs/
│   ├── WEEK-1/
│   │   ├── README.md
│   │   ├── Class-1.md
│   │   └── Class-2.md
│   ├── WEEK-2/
│   │   ├── README.md
│   │   ├── Class-1.md
│   │   └── Class-2.md
│   └── ... WEEK-3 through WEEK-7
├── week-01-spring-core-demo/
├── week-01-employee-management/
├── week-02-employee-management/    ← continues EMS in Week 2
├── week-03-expense-approval/
└── week-04-booking-service/
```

### Why one repo?

| Benefit | Why it matters |
|---------|----------------|
| **One link for students** | Share once on Day 1 — no confusion |
| **Git history tells a story** | Interviewers see progression week by week |
| **Easy to pull updates** | `git pull` gets new week's docs + code |
| **You manage one place** | Push after each class, tag releases per week |
| **Fork-friendly** | 100+ students fork once, pull weekly |

### How to add a new week

1. Create `docs/WEEK-N/` with `README.md`, `Class-1.md`, `Class-2.md`
2. Add code folder `week-0N-project-name/`
3. Commit: `git commit -m "Week N: Class 1 + Class 2 materials"`
4. Optional tag: `git tag week-2` for students who joined late

### Git workflow per class

```
Before class (you):     git push main  →  students git pull
After class (you):      push solution code + updated docs
Students:               fork once → pull every Saturday
```

---

## ❌ Not recommended: New repo every week

| Problem | Detail |
|---------|--------|
| Link chaos | Students bookmark wrong repo |
| Broken history | Can't see learning journey in one place |
| More admin | 7 repos to create, permission, README each |
| Portfolio split | 3 projects scattered across repos |

### When a separate repo makes sense

- **Student's personal fork** — they fork YOUR one repo (recommended)
- **Private solutions repo** — optional `codekerdos-bootcamp-solutions` (private, for TAs only)
- **Archived cohort** — after bootcamp ends, tag `cohort-june-2026` on main repo

---

## Folder naming convention

| Type | Pattern | Example |
|------|---------|---------|
| Week docs | `docs/WEEK-N/Class-M.md` | `docs/WEEK-2/Class-1.md` |
| Code project | `week-0N-project-name/` | `week-03-expense-approval/` |
| Shared resources | `docs/groq-setup.md` | stays in `docs/` root |

---

## GitHub releases (optional, professional touch)

After each week, create a GitHub Release:

```
Week 1 Release — Spring Core + First Groq Call
  Tag: week-1
  Assets: postman collection, slides (if any)
```

Students who miss a week can checkout that tag:

```bash
git checkout week-1
```

---

## Summary

| Question | Answer |
|----------|--------|
| New repo each week? | **No** — one repo, weekly folders |
| Where do docs live? | `docs/WEEK-N/Class-1.md` and `Class-2.md` |
| Where does code live? | `week-0N-project-name/` at repo root |
| Student copies? | **Fork** the one repo, `git pull` weekly |
| When bootcamp ends? | Tag final release, students pin on LinkedIn |

---

*CodeKerdos.in · Spring Boot + AI Bootcamp*
